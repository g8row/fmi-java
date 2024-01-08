package bg.sofia.uni.fmi.mjt.udemy.account;

import bg.sofia.uni.fmi.mjt.udemy.account.type.AccountType;

public class EducationalAccount extends AccountBase{
    public EducationalAccount(String username, double balance) {
        super(username, balance, AccountType.EDUCATION);
    }
}
