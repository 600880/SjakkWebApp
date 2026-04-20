package com.example.sjakkwebapp.util;

import com.example.sjakkwebapp.service.SpillService;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SessionEventListener implements HttpSessionListener {

    @Autowired
    private SpillService spillService;

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        String bruker = (String) se.getSession().getAttribute("bruker");
        if (bruker != null) {
            spillService.endMatch(bruker);
        }
        spill.Parti parti = (spill.Parti) se.getSession().getAttribute("parti");
        if (parti != null) {
            parti.stop();
        }
    }
}
