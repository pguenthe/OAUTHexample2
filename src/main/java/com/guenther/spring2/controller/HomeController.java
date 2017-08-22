package com.guenther.spring2.controller;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;


@Controller
public class HomeController {
    //step 1: Have the user login and get a code
    @RequestMapping("/")
    public ModelAndView oauth1() {
        System.out.println("Running / " + Credentials.CLIENT_ID );
        ModelAndView mv = new ModelAndView ("oauth1", "clientID",
                Credentials.CLIENT_ID);
        //mv.addObject("redirect", "http://localhost:8080/oauth2");
        mv.addObject("redirect", Credentials.REDIRECT_URI);
        return mv;
    }

    @RequestMapping("/oauth2")
    public ModelAndView oauth2(@RequestParam("code") String code,
                               @RequestParam("state") String state) {
        System.out.println("Received code back: " + code);

        try {
            //step 2: exchange the code for a token
            if (code == null) {
                return new ModelAndView("error", "errmsg", "Code was null");
            }

            //this HttpCLient will make requests from the other server
            HttpClient http = HttpClientBuilder.create().build();

            String getURL = "https://graph.facebook.com/v2.10/oauth/access_token?";
            getURL += "client_id="+ Credentials.CLIENT_ID + "&";
            getURL += "redirect_uri=" + Credentials.REDIRECT_URI + "&";
            getURL += "client_secret=" + Credentials.CLIENT_SECRET + "&";
            getURL +=  "code=" + code;
            HttpGet getPage = new HttpGet(getURL);

            //actually run it and pull in the response
            HttpResponse resp = http.execute(getPage);

            int status = resp.getStatusLine().getStatusCode();
            //get the actual content from inside the response
            String response = EntityUtils.toString(resp.getEntity());

            JSONObject json = new JSONObject(response);
            String token = json.getString("access_token");

            String url2 = "https://graph.facebook.com/v2.10/me";
            url2 += "?access_token=" + token;
            HttpGet getPage2 = new HttpGet(url2);
            HttpResponse resp2 = http.execute(getPage2);

            int status2 = resp2.getStatusLine().getStatusCode();
            //get the actual content from inside the response
            String response2 = EntityUtils.toString(resp2.getEntity());

            JSONObject json2 = new JSONObject(response2);
            String name = json2.getString("name");
            String id = json2.getString("id");

            //returning lots of debugging/demo info
            ModelAndView mv = new ModelAndView("oauth2", "code", code);
            mv.addObject ("status", status);
            mv.addObject ("response", response);
            mv.addObject("token", token);//for debugging
            mv.addObject ("status2", status2);
            mv.addObject("response2", response2);

            //in real life, I'd only add these two
            mv.addObject("name", name);
            mv.addObject("id", id);

            return mv;

        } catch (IOException e) {
            e.printStackTrace();
            return new ModelAndView("error", "errmgs", e.getStackTrace());
        }catch (JSONException e){
            e.printStackTrace();
            return new ModelAndView("error", "errmgs", e.getStackTrace());
        }
    }
}