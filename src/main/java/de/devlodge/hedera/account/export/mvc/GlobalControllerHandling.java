package de.devlodge.hedera.account.export.mvc;

import de.devlodge.hedera.account.export.session.SessionStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerHandling {

    private final SessionStore transactionService;

    @Autowired
    public GlobalControllerHandling(SessionStore transactionService) {
        this.transactionService = transactionService;
    }

    @ModelAttribute("accountId")
    public String accountId() {
        return transactionService.getAccountId();
    }
}
