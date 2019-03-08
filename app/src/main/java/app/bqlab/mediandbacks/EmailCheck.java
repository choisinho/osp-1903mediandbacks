package app.bqlab.mediandbacks;

class EmailCheck {
    static boolean isCorrect(String email) {
        String validCharacters = "abcdefghijklmnopqrstuvwxyz0123456789@.";
        for (int i = 0; i < email.length(); i++) {
            for (int j = 0; j < validCharacters.length(); j++) {
                if (email.charAt(i) == validCharacters.charAt(j))
                    break;
                if (j == validCharacters.length() - 1 && email.charAt(i) != validCharacters.charAt(j))
                    return false;
            }
        }
        return email.contains("@") && email.contains(".");
    }
}
