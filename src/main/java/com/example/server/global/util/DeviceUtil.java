package com.example.server.global.util;

import jakarta.servlet.http.HttpServletRequest;

public class DeviceUtil {

    private static final String MOBILE = "MOBILE";
    private static final String WEB = "WEB";

    public static String getDevice(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null) {
            boolean isMobile = userAgent.matches(".*(iPhone|iPad|Android|LG|SAMSUNG|Windows CE|BlackBerry|Mobile).*");
            if (isMobile) {
                return MOBILE;
            }

        }
        return WEB;
    }

}
