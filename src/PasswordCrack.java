import java.io.*;
import java.util.ArrayList;

/**
 * Klassen tar en ordlista och en lista med hashade lösenord som argument och
 * returnerar lösenorden i klartext
 * Created by mikaelnorberg on 2017-05-09.
 */
public class PasswordCrack {
    private ArrayList<String> dictionary;

    private ArrayList<User> users;
    private ArrayList<User> crackedUsers;

    private StringBuilder sb;


    private class User {
        private String userName;

        private String salt;
        private String hash;
        private String saltAndHash;
        private String password;

        private String fullName;
        private String firstName;
        private String middleName;
        private String lastName;

        private boolean cracked;


        public User(String user) {
            this.cracked = false;
            getAttributes(user);

        }

        private void getAttributes(String user) {
            String[] temp = user.split(":");
            if(temp.length != 7){
                userInputError(0);
            }
            for (String info : temp) {
                if(info.isEmpty()) {
                    userInputError(1);
                }
            }
            userName(temp[0]);
            password(temp[1]);
            name(temp[4]);
        }

        private void userInputError(int error){
            System.out.println();
            if (error == 0) {
                System.out.println("Användarinformationen måste ha följande format:");
                System.out.println("account:encrypted password data:uid:gid:GCOS-field:homedir:shell");
            } else if (error == 1) {
                System.out.println("Alla fält i account:encrypted password data:uid:gid:GCOS-field:homedir:shell");
                System.out.println("måste ha minst ett tecken");
            } else if(error == 2) {
                System.out.println("Lösenord och salt måste tillsammans vara 13 tecken.");
            }
            System.out.println("Försök igen. Programmet avslutas.");
            System.out.println();
            System.exit(0);
        }

        private void userName(String userName) {
            this.userName = userName;
        }

        private void password(String password) {
            if(password.length() != 13){
                userInputError(2);
            }
            this.salt = password.substring(0,2);
            this.hash = password.substring(2);
            this.saltAndHash = password;
        }

        private void name(String name) {
            String[] temp;
            temp = name.split(" ");
            this.fullName = name;
            this.firstName = temp[0].toLowerCase();
            if(temp.length == 2) {
                this.lastName = temp[1].toLowerCase();
            }
            if(temp.length == 3) {
                this.middleName = temp[1].toLowerCase();
                this.lastName = temp[2].toLowerCase();
            }
        }
    }

    PasswordCrack (String dictionaryFile, String userFile) {
        this.users = new ArrayList<>();
        this.crackedUsers = new ArrayList<>();
        this.sb = new StringBuilder();

        this.dictionary = readFile(dictionaryFile);

        for(String userInfo : readFile(userFile)){
            this.users.add(new User(userInfo));
        }
        if(this.users.isEmpty()){
            System.out.println();
            System.out.println("passwordfilen innhehöll inga användare.");
            System.out.println("Försök igen. Programmet avslutas.");
            System.out.println();
            System.exit(0);
        }
        crack("");
        //seed();
        //seedCommonWords();
        if(this.dictionary.isEmpty()) {
            bruteForce();
        } else if (this.dictionary.size() <= 1000) {
            methodCaller1();
        } else {
            methodCaller();
        }
	System.out.println("nu blir det bruteforce");
        bruteForce();
        //writeFile();
    }

    private void seed() {
        for (User user : this.users) {
            if(user.firstName != null) {
                if (user.firstName.length() < 3) {
                    if(!user.firstName.contains(".")) {
                        this.dictionary.add(0, user.firstName);
                    }
                } else if(user.firstName.length() > 2){
                    this.dictionary.add(0, user.firstName);
                }
            }
            if(user.lastName != null) {
                if (user.lastName.length() < 3) {
                    if(!user.lastName.contains(".")) {
                        this.dictionary.add(0, user.lastName);
                    }
                } else if(user.lastName.length() > 2){
                    this.dictionary.add(0, user.lastName);
                }
            }
            if(user.middleName != null) {
                if (user.middleName.length() < 3) {
                    if(!user.middleName.contains(".")) {
                        this.dictionary.add(0, user.middleName);
                    }
                } else if(user.middleName.length() > 2){
                    this.dictionary.add(0, user.middleName);
                }
            }
        }
        this.sb.setLength(0);
    }
    private void seedKnownCrackedPasswords(){
        String[] words = {"quincy", "chandler", "fireboat", "football", "password", "smoke", "toothbrush",
                "isbound", "utti", "serahswolp", "yliffuns", "Otis", "Tiers", "aJIVA", "PANELLO", "etarudb", "nfield",
                "ltearoom", "taksj"};
        for (String w : words) {
            this.dictionary.add(w);
        }
    }
    private void seedCommonWords(){
        String[] words = {"12345", "12345678", "qwerty", "letmein", "abc123", "111111", "michael", "login", "admin",
                "passw0rd", "hottie", "loveme", "zaq1zaq", "google", "mynoob", "kth", "qwertyuiop", "18atcskd2w",
                "1q2w3e4r5t", "zxcvbnm", "1q2w3e4"};
        for (String w : words) {
            this.dictionary.add(w);
        }
    }


    private void methodCaller1() {
        ArrayList<String> oneMangle = new ArrayList<>();
        ArrayList<String> prepend = new ArrayList<>();
        ArrayList<String> append = new ArrayList<>();


        ArrayList<String>twoMangle = new ArrayList<>();
        ArrayList<String> prepend2 = new ArrayList<>();
        ArrayList<String> append2 = new ArrayList<>();


        //System.out.println("dictionary word");
        for (String word : this.dictionary) {
            crack(word);
        }
        //System.out.println("dictionary word");

        //System.out.println("oneMangle");
        for (int algoritm = 0; algoritm < 12; algoritm++) {
            for (String word : mangleList(algoritm, this.dictionary)) {
                crack(word);
                oneMangle.add(word);
            }
        }

        for (String word : mangleList(13, this.dictionary)) {
            crack(word);
            prepend.add(word);
        }
        for (String word : mangleList(12, this.dictionary)) {
            crack(word);
            append.add(word);
        }
        //System.out.println("oneMangle");


        //System.out.println("twoMangle");
        for (int algoritm = 0; algoritm < 12; algoritm++) {
            for (String word : mangleList(algoritm, oneMangle)) {
                crack(word);
                twoMangle.add(word);
            }
        }
        for (String word : mangleList(12, oneMangle)) {
            crack(word);
            append2.add(word);
        }
        for (String word : mangleList(13, oneMangle)) {
            crack(word);
            prepend2.add(word);
        }




        for (String word : mangleList(0, append)) {
            crack(word);
            twoMangle.add(word);
        }
        for (int algoritm = 2; algoritm < 12; algoritm++) {
            for (String word : mangleList(algoritm, append)) {
                crack(word);
                twoMangle.add(word);
            }
        }
        for (String word : mangleList(12, append)) {
            crack(word);
            append2.add(word);
        }
        for (String word : mangleList(13, append)) {
            crack(word);
            prepend2.add(word);
        }




        for (int algoritm = 1; algoritm < 12; algoritm++) {
            for (String word : mangleList(algoritm, prepend)) {
                crack(word);
                prepend2.add(word);
            }
        }
        for (String word : mangleList(12, prepend)) {
            crack(word);
            append2.add(word);
        }
        for (String word : mangleList(13, prepend)) {
            crack(word);
            prepend2.add(word);
        }
        /*
        System.out.println("twoMangle");
        System.out.println("onemangle " + oneMangle.size());
        System.out.println("prepend " + prepend.size());
        System.out.println("append " + append.size());
        System.out.println("twomangle " + twoMangle.size());
        System.out.println("prepend2 " + prepend2.size());
        System.out.println("append2 " + append2.size());
        */
        //System.out.println("threeMangle");

        for (int algoritm = 0; algoritm < 12; algoritm++) {
            for (String word : twoMangle) {
                crack(mangle(algoritm, word));
            }
        }
        for (String word : twoMangle) {
            prepend(word);
        }
        for (String word : twoMangle) {
            append(word);
        }




        for (String word : append2) {
            crack(deleteFirst(word));
        }

        for (int algoritm = 2; algoritm < 12; algoritm++) {
            for (String word : append2) {
                crack(mangle(algoritm, word));
            }
        }
        for (int algoritm = 1; algoritm < 12; algoritm++) {
            for (String word : prepend2) {
                mangle(algoritm, word);
            }
        }

        if(this.dictionary.size() < 40)
            threePend(append2, prepend2);
    }

    private void threePend(ArrayList<String> append2, ArrayList<String> prepend2){
        for (String word : append2) {
            append(word);
        }
        for (String word : append2) {
            prepend(word);
        }


        for (String word : prepend2) {
            prepend(word);
        }
        for (String word : prepend2) {
            append(word);
        }
    }


    private ArrayList<String> mangleList(int algorithm, ArrayList<String> mangles) {
        ArrayList<String> manglesToBe = new ArrayList<>();
        for (String word : mangles) {
            switch (algorithm) {
                case 0:
                    manglesToBe.add(deleteFirst(word));
                    break;
                case 1:
                    manglesToBe.add(deleteLast(word));
                    break;
                case 2:
                    manglesToBe.add(reverse(word));
                    break;
                case 3:
                    manglesToBe.add(duplicate(word));
                    break;
                case 4:
                    manglesToBe.add(reflect1(word));
                    break;
                case 5:
                    manglesToBe.add(reflect2(word));
                    break;
                case 6:
                    manglesToBe.add(uppercase(word));
                    break;
                case 7:
                    manglesToBe.add(lowercase(word));
                    break;
                case 8:
                    manglesToBe.add(capitalize(word));
                    break;
                case 9:
                    manglesToBe.add(ncapitalize(word));
                    break;
                case 10:
                    manglesToBe.add(toggle(word, true));
                    break;
                case 11:
                    manglesToBe.add(toggle(word, false));
                    break;
                case 12:
                    for (String w : append1(word)) {
                        manglesToBe.add(w);
                    }
                    break;
                default:
                    for (String w : prepend1(word)) {
                        manglesToBe.add(w);
                    }
                    break;
            }
        }
        return manglesToBe;
    }
    private void methodCaller() {
        String w;
        ArrayList<String> oneMangle = new ArrayList<>();

        for (String word : this.dictionary) {
            crack(word);
        }

        for (String word : this.dictionary) {
            for (int algoritm = 0; algoritm < 12; algoritm++) {
                w = mangle(algoritm, word);
                crack(w);
                oneMangle.add(w);
            }
        }

        //System.out.println("prepend");
        for (String word : this.dictionary) {
            prepend(word);
        }
        //System.out.println("prepend");

        //System.out.println("append");
        for (String word : this.dictionary) {
            append(word);
        }
        //System.out.println("append");

        //System.out.println("två mangle");
        for (String word : oneMangle) {
            for (int i = 0; i < 12; i++) {
                crack(mangle(i, word));
            }
        }
        //System.out.println("två mangle");

        //System.out.println("append med en mangle");
        for (String word : oneMangle) {
            append(word);
        }
        //System.out.println("append med en mangle");

        //System.out.println("prepend med en mangle");
        for (String word : oneMangle) {
            prepend(word);
        }
        //System.out.println("prepend med en mangle");

        //System.out.println("tre mangle");
        for (String word : oneMangle) {
            for (int i = 0; i < 12; i++) {
                for (int j = 0; j < 12; j++) {
                    crack(mangle(i, mangle(j, word)));
                }
            }
        }
        //System.out.println("tre mangle");

        //System.out.println("prepend med två mangle");
        for (String word : oneMangle) {
            for (int i = 0; i < 12; i++) {
                prepend(mangle(i, word));
            }
        }
        //System.out.println("prepend med två mangle");
        //System.out.println("append med två mangle");
        for (String word : oneMangle) {
            for (int i = 0; i < 12; i++) {
                append(mangle(i, word));
            }
        }
        //System.out.println("append med två mangle");
    }


    private String mangle(int algorithm, String word) {
        switch (algorithm) {
            case 0:
                return deleteFirst(word);
            case 1:
                return deleteLast(word);
            case 2:
                return reverse(word);
            case 3:
                return duplicate(word);
            case 4:
                return reflect1(word);
            case 5:
                return reflect2(word);
            case 6:
                return uppercase(word);
            case 7:
                return lowercase(word);
            case 8:
                return capitalize(word);
            case 9:
                return ncapitalize(word);
            case 10:
                return toggle(word, true);
            default:
                return toggle(word, false);
        }
    }









    private void crack() {

        for (String word : this.dictionary) {
            //quincy:LN6yWF5o0tJMQ:511:511:Quincy Adams Wagstaff:/home/quincy:/bin/zsh password: quincy
            //arabella:pH/sQQ.tHXd0.:505:505:Arabella Rittenhouse:/home/arabella:/bin/zsh password: chandler
            //hugo:nLjRsgLNiZlMc:517:517:Hugo Z. Hackenbush:/home/hugo:/bin/zsh password: fireboat
            //rosa:Ev.amPxaQmVTw:510:510:Rosa Castaldi:/home/rosa:/bin/bash password: football
            //frank:tHe4RHuY2YMrg:513:513:Frank Wagstaff:/home/frank:/bin/bash password: password
            //sam:MpWKF7IexnazI:515:515:Sam Grunion:/home/sam:/bin/bash password: smoke
            //antonio:Kz/xV6nl1C.TA:520:520:Antonio Pirelli:/home/antonio:/bin/zsh password: toothbrush
            crack(word);
        }
        for (String word : this.dictionary) {

            //rusty:oU1WrDYnx1B8M:504:504:Rusty Panello:/home/rusty:/bin/bash password: isbound
            //bob:YkVmHcrS4tAUA:503:503:Bob Roland:/home/bob:/bin/bash password: utti
            //quentin:qvTSR5Goqo/26:512:512:S. Quentin Quale:/home/quentin:/bin/ksh password: nfield
            crack(deleteFirst(word));

            //sammy:eK12rYLtp4A52:518:518:Sammy Brown:/home/sammy:/bin/zsh password: serahswolp
            //cheever:ZooXyeZoRsmdo:502:502:J. Cheever Loophole:/home/cheever:/bin/bash password: yliffuns
            crack(reverse(word));

            //otis:I4clveO3l8Bnw:509:509:Otis B. Driftwood:/home/otis:/bin/bash password: Otis
            //ronald:8NMppxfpAZqIM:516:516:Ronald Kornblow:/home/ronald:/bin/bash password: Tiers
            crack(capitalize(word));

            //horatio:N2.65E10Y7gpk:506:506:Horatio Jamison:/home/horatio:/bin/bash password: aJIVA
            crack(ncapitalize(word));

            //joe:KI3XYGWt2iB9.:501:501:Joe Panello:/home/joe:/bin/ksh password: PANELLO
            crack(uppercase(word));

            //peter:Vgg3Y1qxA1T8o:507:507:Peter Minuit:/home/peter:/bin/bash password: etarudb
            crack(reverse(deleteFirst(word)));

            //emile:5diFKQFKA0F8c:514:514:Emile J. Keck:/home/emile:/bin/zsh password: NITNELKS
            crack(deleteFirst(reverse(uppercase(word))));
        }


        for (String word : this.dictionary) {
            //emily:zK/NDuNVgUuaw:519:519:Emily Upjohn:/home/emily:/bin/bash password: ltearoom
            for (String w : prepend1(word)
                    ) {
                crack(w);
            }

            //lionel:BLXnCsDZVzlno:508:508:Lionel Q. Devereaux:/home/lionel:/bin/dash password: taksj
            for (String w : append1(reverse(word))) {
                crack(w);
            }
        }
    }
    private void crack(String word) {
        for (User user : this.users) {
            if (user.saltAndHash.compareTo(jcrypt.crypt(user.salt, word)) == 0) {
                user.cracked = true;
                user.password = word;
                System.out.println(user.password);
                if (allCracked()) {
                    System.exit(0);
                }
            }
        }
        moveUserToCrackedUsers();
    }


    private void crackString(int index, int c, char[] candidate){
        if (c <= 9) {
            candidate[index] = (char) (c+48);
        }
        else if (c <= 35) {
            candidate[index] = (char) (c+55);
        }
        else {
            candidate[index] = (char) (c+61);
        }
        crack(new String(candidate));
    }
    private void bruteForce() {
        char[] candidate = new char[1];
        for (int p = 0; p < 62; p++) {
            crackString(0, p, candidate);
        }
        candidate = new char[2];
        for (int o = 0; o < 62; o++) {
            crackString(1, o, candidate);
            for (int p = 0; p < 62; p++) {
                crackString(0, p, candidate);
            }
        }
        candidate = new char[3];
        for (int n = 0; n < 62; n++){
            crackString(2, n, candidate);
            for (int o = 0; o < 62; o++){
                crackString(1, o, candidate);
                for (int p = 0; p < 62; p++){
                    crackString(0, p, candidate);
                }
            }
        }

        candidate = new char[4];
        for (int m = 0; m < 62; m++){
            crackString(3, m, candidate);
            for (int n = 0; n < 62; n++){
                crackString(2, n, candidate);
                for (int o = 0; o < 62; o++){
                    crackString(1, o, candidate);
                    for (int p = 0; p < 62; p++){
                        crackString(0, p, candidate);
                    }
                }
            }
        }


        candidate = new char[5];
        for (int l = 0; l < 62; l++){
            crackString(4, l, candidate);
            for (int m = 0; m < 62; m++){
                crackString(3, m, candidate);
                for (int n = 0; n < 62; n++){
                    crackString(2, n, candidate);
                    for (int o = 0; o < 62; o++){
                        crackString(1, o, candidate);
                        for (int p = 0; p < 62; p++){
                            crackString(0, p, candidate);
                        }
                    }
                }
            }
        }


        candidate = new char[6];
        for (int k = 0; k < 62; k++){
            crackString(5, k, candidate);
            for (int l = 0; l < 62; l++){
                crackString(4, l, candidate);
                for (int m = 0; m < 62; m++){
                    crackString(3, m, candidate);
                    for (int n = 0; n < 62; n++){
                        crackString(2, n, candidate);
                        for (int o = 0; o < 62; o++){
                            crackString(1, o, candidate);
                            for (int p = 0; p < 62; p++){
                                crackString(0, p, candidate);
                            }
                        }
                    }
                }
            }
        }

        candidate = new char[7];
        for (int j = 0; j < 62; j++){
            crackString(6, j, candidate);
            for (int k = 0; k < 62; k++){
                crackString(5, k, candidate);
                for (int l = 0; l < 62; l++){
                    crackString(4, l, candidate);
                    for (int m = 0; m < 62; m++){
                        crackString(3, m, candidate);
                        for (int n = 0; n < 62; n++){
                            crackString(2, n, candidate);
                            for (int o = 0; o < 62; o++){
                                crackString(1, o, candidate);
                                for (int p = 0; p < 62; p++){
                                    crackString(0, p, candidate);

                                }
                            }
                        }
                    }
                }
            }
        }


        candidate = new char[8];
        for (int i = 0; i < 62; i++){
            crackString(7, i, candidate);
            for (int j = 0; j < 62; j++){
                crackString(6, j, candidate);
                for (int k = 0; k < 62; k++){
                    crackString(5, k, candidate);
                    for (int l = 0; l < 62; l++){
                        crackString(4, l, candidate);
                        for (int m = 0; m < 62; m++){
                            crackString(3, m, candidate);
                            for (int n = 0; n < 62; n++){
                                crackString(2, n, candidate);
                                for (int o = 0; o < 62; o++){
                                    crackString(1, o, candidate);
                                    for (int p = 0; p < 62; p++){
                                        crackString(0, p, candidate);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }



    private boolean allCracked() {
        for (User user : this.users) {
            if(!user.cracked) {
                return false;
            }
        }
        return true;
    }

    private void moveUserToCrackedUsers() {
        for(int i = 0; i < this.users.size(); i++) {
            if(this.users.get(i).cracked) {
                this.crackedUsers.add(this.users.get(i));
                this.users.remove(i);
            }
        }
    }


    private ArrayList<String> prepend1(String word) {
        ArrayList<String> temp = new ArrayList<>();
        for(int i = 48; i <= 57; i++) {
            this.sb.setLength(0);
            temp.add(this.sb.append((char)i).append(word).toString());
        }
        for(int i = 65; i <= 90; i++) {
            this.sb.setLength(0);
            temp.add(this.sb.append((char)i).append(word).toString());
        }
        for(int i = 97; i <= 122; i++) {
            this.sb.setLength(0);
            temp.add(this.sb.append((char)i).append(word).toString());
        }
        return temp;
    }
    private ArrayList<String> append1(String word) {
        ArrayList<String> temp = new ArrayList<>();
        if(word.length() < 8) {
            for (int i = 48; i <= 57; i++) {
                this.sb.setLength(0);
                temp.add(this.sb.append(word).append((char) i).toString());
            }
            for (int i = 65; i <= 90; i++) {
                this.sb.setLength(0);
                temp.add(this.sb.append(word).append((char) i).toString());
            }
            for (int i = 97; i <= 122; i++) {
                this.sb.setLength(0);
                temp.add(this.sb.append(word).append((char) i).toString());
            }
        }
        return temp;
    }
    private void prepend(String word) {
        for(int i = 48; i <= 57; i++) {
            this.sb.setLength(0);
            crack(this.sb.append((char)i).append(word).toString());
        }
        for(int i = 65; i <= 90; i++) {
            this.sb.setLength(0);
            crack(this.sb.append((char)i).append(word).toString());
        }
        for(int i = 97; i <= 122; i++) {
            this.sb.setLength(0);
            crack(this.sb.append((char)i).append(word).toString());
        }
    }
    private void append(String word) {
        if(word.length() < 8) {
            for (int i = 48; i <= 57; i++) {
                this.sb.setLength(0);
                crack(this.sb.append(word).append((char) i).toString());
            }
            for (int i = 65; i <= 90; i++) {
                this.sb.setLength(0);
                crack(this.sb.append(word).append((char) i).toString());
            }
            for (int i = 97; i <= 122; i++) {
                this.sb.setLength(0);
                crack(this.sb.append(word).append((char) i).toString());
            }
        }
    }
    private String deleteFirst(String word) {
        if(word.length() > 1) {
            sb.setLength(0);
            return this.sb.append(word).deleteCharAt(0).toString();
        }
        return word;
    }
    private String deleteLast(String word) {
        if(word.length() > 1) {
            sb.setLength(0);
            return this.sb.append(word).deleteCharAt(word.length() - 1).toString();
        }
        return word;
    }
    private String reverse(String word) {
        this.sb.setLength(0);
        return this.sb.append(word).reverse().toString();
    }
    private String duplicate(String word) {
        if(word.length() < 8) {
            this.sb.setLength(0);
            return this.sb.append(word).append(word).toString();
        }
        return word;
    }
    private String reflect1(String word) {
        if(word.length() < 8) {
            this.sb.setLength(0);
            return this.sb.append(word).reverse().append(word).toString();
        }
        return word;
    }
    private String reflect2(String word) {
        if(word.length() < 8) {
            this.sb.setLength(0);
            StringBuilder b = new StringBuilder(word);
            b.reverse();
            return this.sb.append(word).append(b).toString();
        }
        return word;
    }
    private String uppercase(String word) {
        return word.toUpperCase();
    }
    private String lowercase(String word) {
        return word.toLowerCase();
    }
    private String capitalize(String word) {
        int i = (int)word.charAt(0);
        if(i > 90){
            this.sb.setLength(0);
            return this.sb.append(word).deleteCharAt(0).insert(0, (char)(i-32)).toString();
        }
        return word;
    }
    private String ncapitalize(String word) {
        int i = (int)word.charAt(0);
        this.sb.setLength(0);
        return this.sb.append(uppercase(word)).deleteCharAt(0).insert(0, (char)i).toString();
    }
    private String toggle(String word, boolean uppercase) {
        this.sb.setLength(0);
        int length = word.length();
        String upper = uppercase(word);
        String lower = lowercase(word);
        for(int i = 0; i < length; i++) {
            if (uppercase) {
                this.sb.append(upper.charAt(i));
                uppercase = false;
            } else {
                this.sb.append(lower.charAt(i));
                uppercase = true;
            }
        }
        return this.sb.toString();
    }

    private ArrayList<String> readFile(String fileName) {
        ArrayList<String> temp = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                if(!line.isEmpty())
                    temp.add(line);
            }
        } catch (FileNotFoundException e){
            System.out.println("Kontrollera filens existens och läsrättigheter och försök igen.");
            System.out.println("Programmet avslutas.");
            System.exit(0);
        } catch (IOException e){
            System.out.println("Kontrollera filens existens, format och läsrättigheter och försök igen.");
            System.out.println("Programmet avslutas.");
            System.exit(0);
        }
        return temp;
    }

    private void writeFile(){
        try(PrintWriter out = new PrintWriter("passwd2-plain.txt")){
            for (User user : this.crackedUsers
                 ) {
                out.println(user.password);
            }
        } catch (FileNotFoundException e){

        }
    }

    public static void main(String[] args) {
        if (args.length == 2) {
            PasswordCrack pc = new PasswordCrack(args[0], args[1]);
        } else {
            System.out.println("Programmet måste ha en fil med användare och en fil med ordlista. Om du vill att");
            System.out.println("programmet ska testa alla möjliga kombinationer av strängar upp till längden 8");
            System.out.println("utan en ordlista så ange en tom men existerande fil.");
            System.out.println("kör programmet på följande vis:");
            System.out.println("java PasswordCrack <dictionary> <passwd>");
            System.out.println("Försök igen. Programmet avslutas.");
        }
    }
}
