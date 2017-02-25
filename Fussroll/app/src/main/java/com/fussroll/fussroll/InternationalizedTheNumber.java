package com.fussroll.fussroll;

import android.content.SharedPreferences;

class InternationalizedTheNumber {

    private String number, owner;

    InternationalizedTheNumber(String number, String owner) {
        this.number = number;
        this.owner = owner;
    }

    String getIt() {

        //Country Specific --- ######### (India, US, UK, Canada)
        if(number.length() == 11 && number.substring(0, number.length()-10).equals("0")) {
            //Same country, I presume
            number = owner.substring(0, owner.length()-10)+number.substring(number.length()-10);
        }
        else if(number.length() > 10 && number.length() <=13 && number.indexOf('+') == -1) {
            //User didn't add the '+' symbol in front of country code
            number = "+"+number;
        }
        else if(number.length() == 10) {
            //Probably number exists in the same country as the user does
            number = owner.substring(0, owner.length()-10)+number;
        }

        return number;

    }

}
