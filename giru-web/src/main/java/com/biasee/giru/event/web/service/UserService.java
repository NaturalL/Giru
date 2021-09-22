package com.biasee.giru.event.web.service;

import com.biasee.giru.event.web.service.dto.ClientUser;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    public ClientUser getLoginUser() {
        return new ClientUser("default", "1");
    }
}