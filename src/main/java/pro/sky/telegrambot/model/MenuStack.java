package pro.sky.telegrambot.model;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.persistence.*;
import java.util.Objects;

@Entity(name = "menustack")
public class MenuStack {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Schema(description = "текстовый пакет пользователя")
    @Column(columnDefinition = "varchar(255) default 'DOG'")
    private String textPackKey;

    @Schema(description = "отправленый текст")
    private String textKey;

    @Schema(description = "отправленое меню пользователя")
    @Column(nullable = false, columnDefinition = "varchar(255) default 'SPECIES_PET_SELECTION_MENU'")
    private String menuState;

    public MenuStack() {
    }

    public MenuStack(User user) {
        this.menuState = "SPECIES_PET_SELECTION_MENU";
        this.textPackKey = "-2057967076";
        this.textKey = "DEFAULT_MENU_TEXT";
        this.user = user;
    }

    public MenuStack(User user, String textKey, String menuState) {
        this(user);
        this.textKey = textKey;
        this.menuState = menuState;
    }

    public MenuStack(User user, String textPackKey, String textKey, String menuState) {
        this(user, textKey, menuState);
        this.textPackKey = textPackKey;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getTextPackKey() {
        return textPackKey;
    }

    public void setTextPackKey(String textPackKey) {
        this.textPackKey = textPackKey;
    }

    public String getTextKey() {
        return textKey;
    }

    public void setTextKey(String textKey) {
        this.textKey = textKey;
    }

    public String getMenuState() {
        return menuState;
    }

    public void setMenuState(String menuState) {
        this.menuState = menuState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (this.getClass() != o.getClass()) {
            return false;
        }
        MenuStack menuStack = (MenuStack) o;
        return Objects.equals(menuStack.getId(), this.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}