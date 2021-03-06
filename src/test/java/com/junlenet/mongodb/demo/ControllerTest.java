package com.junlenet.mongodb.demo;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.context.WebApplicationContext;

import com.lad.controller.AccountSecurityController;
import com.lad.controller.InforController;
import com.lad.controller.LoginController;


/**
 * 功能描述：
 * Version: 1.0
 * Time:2017/7/1
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({"classpath:spring.xml","classpath:spring-mvc.xml"})
public class ControllerTest {

    @Autowired
    private LoginController loginController;

    @Autowired
    private AccountSecurityController accountSecurityController;

    @Autowired
    private InforController inforController;

    private MockMvc mockMvc;

    private String phone = "13051577139";

    @Autowired
    private WebApplicationContext wac;

    private static MockHttpServletRequest request;
    private static MockHttpServletResponse response;


    @Before
    public void setUp(){
        mockMvc = webAppContextSetup(wac).build();
        request = new MockHttpServletRequest();
        request.setCharacterEncoding("UTF-8");
        response = new MockHttpServletResponse();

    }

    @Test
    public void testLogin() throws Exception {
        ResultActions resultActions = mockMvc.perform((get("/login/login-no").param("phone", phone)))
                .andExpect
                (status
                        ().isOk())
                .andDo(print());

        MvcResult mvcResult = resultActions.andReturn();
        String result = mvcResult.getResponse().getContentAsString();
        System.out.println("=====客户端获得反馈数据:" + result);

        String res = accountSecurityController.verification_send( mvcResult.getRequest(), mvcResult.getResponse());
        System.out.println(res);

    }


    @Test
    public void testAcc() throws Exception {
        request.setMethod("GET");
        String res = accountSecurityController.verification_send( request, response);
        System.out.println(res);
    }



    @Test
    public void testInforget() throws Exception {
        request.setMethod("GET");
        String res = loginController.verification_send("15320542105", request, response);
        System.out.println(res);
    }


    @Test
    public void testLoginQuick() throws Exception {
        request.setMethod("GET");
        String res = loginController.login_quick("15320542105","111111", request, response);
        System.out.println(res);
    }





}
