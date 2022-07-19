package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.constants.ButtonsText;
import pro.sky.telegrambot.model.User;
import pro.sky.telegrambot.service.MenuService;
import pro.sky.telegrambot.service.MenuStackService;
import pro.sky.telegrambot.service.ReportService;
import pro.sky.telegrambot.service.impl.UserServiceImpl;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static pro.sky.telegrambot.model.MenuStack.ExpectedMessageType;
import static pro.sky.telegrambot.model.User.Role;

/**
 * Основной класс бота, где происходит обработка входящих обновлений из клиента
 * Имплементирует UpdatesListener в качестве обработчика обновлений
 *
 * @see com.pengrad.telegrambot.UpdatesListener
 */
@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    /**
     * Инжекстированный сервис-класс, отвечающий за обработку и создание клавиатур
     *
     * @see MenuService
     */
    private final MenuService menuService;
    /**
     * Сервис репозитория, отвечащий за сохранение пользователей в БД
     *
     * @see UserServiceImpl
     */
    private final UserServiceImpl userService;
    /**
     * Инжекстированный сервис-класс, отвечающий за отслеживанием положения
     * пользователей в БД
     *
     * @see MenuService
     */
    private final MenuStackService menuStackService;
    /**
     * Логгер для класса
     */
    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);
    /**
     * поле инжектирует Телеграм-бота
     *
     * @see pro.sky.telegrambot.configuration.TelegramBotConfiguration
     */
    private final TelegramBot telegramBot;
    private final ReportService reportService;
    /**
     * директория с файлом - схемой проезда к приюту
     */
    java.io.File address = new File("src/main/resources/MapPhoto/address.png");

    /**
     * конструктор класса
     *
     * @param telegramBot      Телеграм бот
     * @param menuService      обработчик-меню
     * @param userService      сервис репозитория пользоваьелей
     * @param menuStackService сервис репозитория положения юезера в меню
     */
    public TelegramBotUpdatesListener(TelegramBot telegramBot,
                                      MenuService menuService,
                                      UserServiceImpl userService,
                                      MenuStackService menuStackService,
                                      ReportService reportService) {
        this.telegramBot = telegramBot;
        this.menuService = menuService;
        this.userService = userService;
        this.reportService = reportService;
        this.menuStackService = menuStackService;
    }

    /**
     * Метод, запускающий (инициализирующий обработчик обновлений)
     */
    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    /**
     * Основной метод класса, в котором происходит обработка обновлений
     *
     * @param updates Обновления, поступившие от бота
     * @return подтверждение обработки обновлений
     */
    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);
            Message message = (update.message() != null) ? update.message() : update.callbackQuery().message();
            User currentUser = userService.getUserByMessage(message);
            Role roleCurrentUser = currentUser.getRole();
            ExpectedMessageType expectFromCurrentUser = menuStackService.getCurrentExpectedMessageTypeByUser(currentUser);
            String textPackKey = menuStackService.getTextPackKeyByUser(currentUser);
            ButtonsText buttonsText = ButtonsText.getButtonText();
            menuStackService.createMenuStack(currentUser, textPackKey);

            if (expectFromCurrentUser == ExpectedMessageType.DIALOG) {
                logger.info("==== Processing update with callback: {}", message.text());
                telegramBot.execute(menuService.sendTextLoader(currentUser.getCompanion(), message.text()));
            }

            try {
                if (expectFromCurrentUser == ExpectedMessageType.COMMAND_OR_TEXT && update.callbackQuery() != null
                        || (update.message() != null && update.message().photo() == null)) {
                    Function<String, Boolean> whatIsMenu = (someButtonNameKey) -> {
                        String someButtonNameValue = buttonsText.getString(someButtonNameKey);
                        String hashSomeButton = menuService.getHashFromButton(someButtonNameValue);
                        return update.callbackQuery().data().equals(hashSomeButton);
                    };
                    BiConsumer<String, String> doSendMessage = (textKey, menuKey) -> {
                        logger.info("==== Processing update with callback: {}", update.callbackQuery().data());
                        menuStackService.saveMenuStackParam(currentUser, textKey, menuKey);
                        String textValue = buttonsText.getString(textKey);
                        List<String> menuValue = buttonsText.getMenu(menuKey);
                        telegramBot.execute(menuService.editMenuLoader(update, textValue, menuValue));
                    };
                    Consumer<File> doSendPhoto = (filePath) -> {
                        logger.info("==== Processing update with callback: {}", update.callbackQuery().data());
                        telegramBot.execute(menuService.sendPhotoLoader(update, filePath));
                    };
                    BiConsumer<Float, Float> goSendLocation = (latitude, longitude) -> {
                        logger.info("==== Processing update with callback: {}", update.callbackQuery().data());
                        telegramBot.execute(menuService.sendLocationLoader(update, latitude, longitude));
                    };
                    if (update.message() != null) {
                        whatIsMenu = (someButtonName) -> message.text().equals(buttonsText.getString(someButtonName));
                        doSendMessage = (textKey, menuKey) -> {
                            logger.info("==== Processing update with message: {}", message.text());
                            List<String> menuValue = buttonsText.getMenu(menuKey);
                            String textValue = buttonsText.getString(textKey);
                            telegramBot.execute(menuService.menuLoader(message, textValue, menuValue));
                        };
                    }
                    if (roleCurrentUser.equals(Role.USER) || roleCurrentUser.equals(Role.PARENT)) {
                        handleUserMessages(whatIsMenu, doSendMessage, doSendPhoto, goSendLocation, currentUser, update, buttonsText);
                    } else if (roleCurrentUser.equals(Role.VOLUNTEER)) {
                        handleVolunteerMessages(whatIsMenu, doSendMessage);
//                } else if (roleCurrentUser.equals(Role.ADMIN)) {
//
                    }
                } else if (message.photo() != null && expectFromCurrentUser == ExpectedMessageType.PIC) {
                    reportService.getPictureFromMessage(message.from().id(), message);
                }

            } catch (Exception e) {
                logger.warn("==== Exception: ", e);
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    /**
     * Метод, обрабатывающий сообщения и нажатия кнопок от пользователя с ролью USER
     *
     * @param whatIsMenu     функция для попадания в нужную ветку условий
     * @param doSendMessage  биконсьюмер для отправки текста
     * @param doSendPhoto    консьюмер для отправки фото
     * @param doSendLocation биконсьюмер для отправки локации
     */
    public void handleUserMessages(Function<String, Boolean> whatIsMenu,
                                   BiConsumer<String, String> doSendMessage,
                                   Consumer<File> doSendPhoto,
                                   BiConsumer<Float, Float> doSendLocation,
                                   User currentUser, Update update, ButtonsText buttonsText) {
        if (whatIsMenu.apply("START_BUTTON")) {
            doSendMessage.accept("START_TEXT", "SPECIES_PET_SELECTION_MENU");
        } else if (whatIsMenu.apply("BACK_BUTTON")) {
            menuStackService.dropMenuStack(currentUser);
            doSendMessage.accept(menuStackService.getPreviousTextKeyByUser(currentUser),
                    menuStackService.getPreviousMenuStateByUser(currentUser));
        } else if (whatIsMenu.apply("CAT_BUTTON") || whatIsMenu.apply("DOG_BUTTON")) {
            menuStackService.setTextPackKey(currentUser, update.callbackQuery().data());
            buttonsText.changeCurrentTextKey(update.callbackQuery().data());
            doSendMessage.accept("GREETING_TEXT", "MAIN_MENU");
        } else if (whatIsMenu.apply("CHANGE_PET_BUTTON")) {
            doSendMessage.accept("START_TEXT", "SPECIES_PET_SELECTION_MENU");
        } else if (whatIsMenu.apply("INFO_BUTTON")) {
            doSendMessage.accept("INFO_TEXT", "INFO_MENU");
        } else if (whatIsMenu.apply("ABOUT_US_BUTTON")) {
            doSendMessage.accept("ABOUT_US", "BACK_TO_MAIN_MENU");
        } else if (whatIsMenu.apply("CONTACTS_BUTTON")) {
            doSendLocation.accept(51.165973F, 71.403983F);
            doSendPhoto.accept(address);
            doSendMessage.accept("SHELTER_CONTACTS", "BACK_TO_MAIN_MENU");
        } else if (whatIsMenu.apply("SAFETY_REGULATIONS_BUTTON")) {
            doSendMessage.accept("SAFETY_REGULATIONS", "BACK_TO_MAIN_MENU");
        } else if (whatIsMenu.apply("SHARE_CONTACT_BUTTON")) {
            doSendMessage.accept("SHARE_CONTACT", "BACK_TO_MAIN_MENU");
        } else if (whatIsMenu.apply("HOW_TO_GET_PET_BUTTON")) {
            doSendMessage.accept("CONSULT_MENU_MESSAGE", "HOW_TO_GET_PET_MENU");
        } else if (whatIsMenu.apply("MEETING_WITH_PET_BUTTON")) {
            doSendMessage.accept("MEETING_WITH_PET", "BACK_TO_MAIN_MENU");
        } else if (whatIsMenu.apply("LIST_OF_DOCUMENTS_BUTTON")) {
            doSendMessage.accept("LIST_OF_DOCUMENTS", "BACK_TO_MAIN_MENU");
        } else if (whatIsMenu.apply("HOW_TO_CARRY_ANIMAL_BUTTON")) {
            doSendMessage.accept("HOW_TO_CARRY_ANIMAL", "BACK_TO_MAIN_MENU");
        } else if (whatIsMenu.apply("MAKING_HOUSE_BUTTON")) {
            doSendMessage.accept("DEFAULT_MENU_TEXT", "MAKING_HOUSE_MENU");
        } else if (whatIsMenu.apply("FOR_PUPPY_BUTTON")) {
            doSendMessage.accept("MAKING_HOUSE_FOR_PUPPY", "BACK_TO_MAIN_MENU");
        } else if (whatIsMenu.apply("FOR_PET_BUTTON")) {
            doSendMessage.accept("MAKING_HOUSE_FOR_PET", "BACK_TO_MAIN_MENU");
        } else if (whatIsMenu.apply("FOR_PET_WITH_DISABILITIES_BUTTON")) {
            doSendMessage.accept("MAKING_HOUSE_FOR_PET_WITH_DISABILITIES", "BACK_TO_MAIN_MENU");
        } else if (whatIsMenu.apply("DOG_HANDLER_ADVICES_BUTTON")) {
            doSendMessage.accept("DOG_HANDLER_ADVICES", "BACK_TO_MAIN_MENU");
        } else if (whatIsMenu.apply("DOG_HANDLERS_BUTTON")) {
            doSendMessage.accept("DOG_HANDLERS", "BACK_TO_MAIN_MENU");
        } else if (whatIsMenu.apply("DENY_LIST_BUTTON")) {
            doSendMessage.accept("DENY_LIST", "BACK_TO_MAIN_MENU");
        } else if (whatIsMenu.apply("CALL_VOLUNTEER_BUTTON")) {
            menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, ExpectedMessageType.DIALOG);
//            handleVolunteerMessages(whatIsMenu, doSendMessage);
            doSendMessage.accept("CALL_VOLUNTEER_TEXT", "BACK_TO_MAIN_MENU");
        } else if (whatIsMenu.apply("BACK_TO_MAIN_MENU_BUTTON")) {
            doSendMessage.accept("DEFAULT_MENU_TEXT", "MAIN_MENU");
        } else {
            doSendMessage.accept("ERROR_COMMAND_TEXT", "MAIN_MENU");
        }
    }

    /**
     * Метод, обрабатывающий сообщения и нажатия кнопок от пользователя с ролью PARENT
     *
     * @param update обновление
     */
    public void handleParentMessages(Update update) {

    }

    /**
     * Метод, обрабатывающий сообщения и нажатия кнопок от пользователя с ролью VOLUNTEER
     *
     * @param whatIsMenu    функция для попадания в нужную ветку условий
     * @param doSendMessage биконсьюмер для отправки текста
     */
    public void handleVolunteerMessages(Function<String, Boolean> whatIsMenu,
                                        BiConsumer<String, String> doSendMessage) {
        if (whatIsMenu.apply("ADD_PARENT_BUTTON")) {
            doSendMessage.accept("ADD_PARENT", "BACK_TO_VOLUNTEERS_MENU");
        } else if (whatIsMenu.apply("CHECK_REPORTS_BUTTON")) {
            doSendMessage.accept("CHECK_REPORTS", "BACK_TO_VOLUNTEERS_MENU");
        }
    }

    /**
     * Метод, обрабатывающий сообщения и нажатия кнопок от пользователя с ролью ADMIN
     *
     * @param update обновление
     */
    public void handleAdminMessages(Update update) {

    }

}
