package com.hardwarestore.controller.api;

import com.hardwarestore.config.CustomUserDetailsService;
import com.hardwarestore.config.JwtUtil;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@AutoConfigureMockMvc(addFilters = false)
public abstract class BaseControllerTest {

    @MockitoBean
    protected JwtUtil jwtUtil;

    @MockitoBean
    protected CustomUserDetailsService userDetailsService;
}
