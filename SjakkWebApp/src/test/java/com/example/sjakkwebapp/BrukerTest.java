package com.example.sjakkwebapp;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.sjakkwebapp.repository.BrukerRepo;
import com.example.sjakkwebapp.service.BrukerService;
import com.example.sjakkwebapp.util.*;
import com.example.sjakkwebapp.model.*;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
public class BrukerTest {
	
    @Autowired
    private MockMvc mockMvc;
	@InjectMocks
	BrukerService s;
	@Mock
	BrukerRepo b;
	
	@Test
	void innloggingsforsok() throws UnsupportedEncodingException, NoSuchAlgorithmException {
		
		// Oppretter brukere/testdata
		MockHttpServletRequest request = new MockHttpServletRequest();
		RedirectAttributes ra;
		ra=Mockito.mock(RedirectAttributes.class);
		
		Bruker bruker = new Bruker();
		bruker.setBruker("bruker@test.no");
		bruker.setPassordsalt("enStrengAvBytes");
		bruker.setPassord(PasswordUtil.passordHashing("¤$[👍]😀&{☠️}~^|‰✅𓁿𓁏😭", "enStrengAvBytes"));
		when(b.findByBruker("bruker@test.no")).thenReturn(bruker);
		
		// Innlogging
		assertTrue(LoginUtil.innloggingsForsok("bruker@test.no", "¤$[👍]😀&{☠️}~^|‰✅𓁿𓁏😭", request, ra, s));
		
	}

    @Test
    void testMinePartier_UtenInnlogging() throws Exception {
        mockMvc.perform(get("/minePartier"))
                .andExpect(status().is4xxClientError());
    }

}
