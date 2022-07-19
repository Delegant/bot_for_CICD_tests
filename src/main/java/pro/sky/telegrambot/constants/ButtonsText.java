package pro.sky.telegrambot.constants;

import java.util.*;

public class ButtonsText {

    public static String HIDDEN_BUTTON;
    private static ButtonsText singletonBundleText;
    private static final String hashPetDefaultTextKey = "-2057967076";
    private final Map<String, ResourceBundle> bundleMap;
    private final Map<String, List<String>> menuMap = new HashMap<>();
    private ResourceBundle bundle;
    private String currentTextKey;

    {
        bundleMap = new HashMap<>();
        bundleMap.put("-55733391", ResourceBundle.getBundle("default", new Locale("cat")));
        bundleMap.put("-2057967076", ResourceBundle.getBundle("default"));
    }

    private ButtonsText() {
        currentTextKey = hashPetDefaultTextKey;
        bundle = bundleMap.get(currentTextKey);
        init();
    }

    public static ButtonsText getButtonText() {
        if (singletonBundleText == null) {
            singletonBundleText = new ButtonsText();
        }
        return singletonBundleText;
    }

    public void changeCurrentTextKey(String newCurrentTextKey) {
        this.currentTextKey = newCurrentTextKey;
        bundle = bundleMap.get(currentTextKey);
        init();
    }

    public String getString(String key) {
        return bundle.getString(key);
    }

    public List<String> getMenu(String key) {
        return menuMap.get(bundle.getString(key));
    }

    private void init() {
        menuMap.put(bundle.getString("SPECIES_PET_SELECTION_MENU"),
                List.of(
                        bundle.getString("CAT_BUTTON"),
                        bundle.getString("DOG_BUTTON")));
        menuMap.put(bundle.getString("BACK_TO_MAIN_MENU"),
                List.of(
                        bundle.getString("BACK_BUTTON"),
                        bundle.getString("BACK_TO_MAIN_MENU_BUTTON")));
        menuMap.put("BACK_TO_VOLUNTEERS_MENU",
                List.of(
                        bundle.getString("BACK_BUTTON"),
                        bundle.getString("BACK_TO_VOLUNTEERS_MENU")
                ));
        menuMap.put(bundle.getString("MAIN_MENU"),
                List.of(
                        bundle.getString("INFO_BUTTON"),
                        bundle.getString("HOW_TO_GET_PET_BUTTON"),
                        bundle.getString("SEND_REPORT_BUTTON"),
                        bundle.getString("CALL_VOLUNTEER_BUTTON"),
                        bundle.getString("CHANGE_PET_BUTTON")
                ));
        menuMap.put(bundle.getString("INFO_MENU"),
                List.of(
                        bundle.getString("ABOUT_US_BUTTON"),
                        bundle.getString("CONTACTS_BUTTON"),
                        bundle.getString("SAFETY_REGULATIONS_BUTTON"),
                        bundle.getString("SHARE_CONTACT_BUTTON"),
                        bundle.getString("CALL_VOLUNTEER_BUTTON"),
                        bundle.getString("BACK_TO_MAIN_MENU_BUTTON")
                ));
        menuMap.put(bundle.getString("HOW_TO_GET_PET_MENU"),
                List.of(
                        bundle.getString("MEETING_WITH_PET_BUTTON"),
                        bundle.getString("LIST_OF_DOCUMENTS_BUTTON"),
                        bundle.getString("HOW_TO_CARRY_ANIMAL_BUTTON"),
                        bundle.getString("MAKING_HOUSE_BUTTON"),
                        bundle.getString("DOG_HANDLER_ADVICES_BUTTON"),
                        bundle.getString("DOG_HANDLERS_BUTTON"),
                        bundle.getString("DENY_LIST_BUTTON"),
                        bundle.getString("BACK_TO_MAIN_MENU_BUTTON")
                ));
        menuMap.put(bundle.getString("MAKING_HOUSE_MENU"),
                List.of(
                        bundle.getString("FOR_PUPPY_BUTTON"),
                        bundle.getString("FOR_PET_BUTTON"),
                        bundle.getString("FOR_PET_WITH_DISABILITIES_BUTTON"),
                        bundle.getString("BACK_BUTTON"),
                        bundle.getString("BACK_TO_MAIN_MENU_BUTTON")
                ));
        menuMap.put(bundle.getString("VOLUNTEER_MAIN_MENU"),
                List.of(
                        bundle.getString("ADD_PARENT"),
                        bundle.getString("CHECK_REPORTS"),
                        bundle.getString("VIEW_INCOMING_MESSAGES"),
                        bundle.getString("TRIAL_PERIOD_FOR_VOLUNTEERS_MENU"),
                        bundle.getString("CONTACT_PARENT")
                ));
        menuMap.put(bundle.getString("TRIAL_PERIOD_MENU"),
                List.of(
                        bundle.getString("APPLY_TRIAL_PERIOD"),
                        bundle.getString("PROLONG_TRIAL_PERIOD"),
                        bundle.getString("DECLINE_TRIAL_PERIOD"),
                        bundle.getString("BACK_TO_VOLUNTEERS_MENU")
                ));
        menuMap.put(bundle.getString("REPORTS_MENU"),
                List.of(
                        bundle.getString("FIND_REPORT_BY_NAME"),
                        bundle.getString("FIND_REPORT_BY_USERID"),
                        bundle.getString("GET_REPORT_BY_ID"),
                        bundle.getString("UNREAD_REPORTS"),
                        bundle.getString("BACK_TO_VOLUNTEERS_MENU")
                ));
        menuMap.put(bundle.getString("ADMIN_MAIN_MENU"),
                List.of(
                        bundle.getString("BACK_BUTTON"),
                        bundle.getString("BACK_TO_VOLUNTEERS_MENU")

                ));
        HIDDEN_BUTTON = bundle.getString("HIDDEN_BUTTON");
    }
}
