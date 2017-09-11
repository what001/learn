package com.what.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.what.domain.GeetestConfig;
import com.what.tools.GeetestLib;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/gt")
public class GeetestController {

    @RequestMapping("/register")
    @ResponseBody
    public JSONObject startCaptcha(HttpServletRequest request){
        String resStr = "{}";
        try {
            GeetestLib gtSdk = new GeetestLib(GeetestConfig.getGeetest_id(), GeetestConfig.getGeetest_key(),
                    GeetestConfig.isnewfailback());

            //自定义userid
            String userid = "10001";

            //进行验证预处理
            int gtServerStatus = gtSdk.preProcess(userid);

            //将服务器状态设置到session中
            request.getSession().setAttribute(gtSdk.gtServerStatusSessionKey, gtServerStatus);
            //将userid设置到session中
            request.getSession().setAttribute("userid", userid);

            resStr = gtSdk.getResponseStr();
        }catch (Exception e){
            e.printStackTrace();
        }
        return JSON.parseObject(resStr);

    }

    @RequestMapping(value = "/ajax-validate", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> verifyLogin(HttpServletRequest request){
        GeetestLib gtSdk = new GeetestLib(GeetestConfig.getGeetest_id(), GeetestConfig.getGeetest_key(),
                GeetestConfig.isnewfailback());

        String challenge = request.getParameter(GeetestLib.fn_geetest_challenge);
        String validate = request.getParameter(GeetestLib.fn_geetest_validate);
        String seccode = request.getParameter(GeetestLib.fn_geetest_seccode);

        //从session中获取gt-server状态
        int gt_server_status_code = (Integer) request.getSession().getAttribute(gtSdk.gtServerStatusSessionKey);

        //从session中获取userid
        String userid = (String)request.getSession().getAttribute("userid");

        int gtResult = 0;

        if (gt_server_status_code == 1) {
            //gt-server正常，向gt-server进行二次验证

            gtResult = gtSdk.enhencedValidateRequest(challenge, validate, seccode, userid);
            System.out.println(gtResult);
        } else {
            // gt-server非正常情况下，进行failback模式验证

            System.out.println("failback:use your own server captcha validate");
            gtResult = gtSdk.failbackValidateRequest(challenge, validate, seccode);
            System.out.println(gtResult);
        }

        System.out.println(gtResult+"qqqqqqqqqqqqqq");

        Map<String, Object> data  = new HashMap<>();

        String status = gtResult == 1 ? "success" : "fail";

        data.put("status", status);
        data.put("version", gtSdk.getVersionInfo());

        return data;
    }
}
