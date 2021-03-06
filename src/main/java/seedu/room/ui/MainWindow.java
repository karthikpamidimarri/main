package seedu.room.ui;

import java.io.File;
import java.util.logging.Logger;

import com.google.common.eventbus.Subscribe;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import seedu.room.commons.core.Config;
import seedu.room.commons.core.GuiSettings;
import seedu.room.commons.core.LogsCenter;
import seedu.room.commons.events.model.EventBookChangedEvent;
import seedu.room.commons.events.model.ResidentBookChangedEvent;
import seedu.room.commons.events.ui.ChangeMonthRequestEvent;
import seedu.room.commons.events.ui.ExitAppRequestEvent;
import seedu.room.commons.events.ui.NewResultAvailableEvent;
import seedu.room.commons.events.ui.PersonPanelSelectionChangedEvent;
import seedu.room.commons.events.ui.ShowHelpRequestEvent;
import seedu.room.commons.events.ui.SwitchTabRequestEvent;
import seedu.room.commons.util.FxViewUtil;
import seedu.room.logic.Logic;
import seedu.room.logic.commands.CommandResult;
import seedu.room.logic.commands.ImportCommand;
import seedu.room.logic.commands.exceptions.CommandException;
import seedu.room.logic.parser.exceptions.ParseException;
import seedu.room.model.UserPrefs;


/**
 * The Main Window. Provides the basic application layout containing
 * a menu bar and space where other JavaFX elements can be placed.
 */
public class MainWindow extends UiPart<Region> {

    private static final String ICON = "/images/resident_book_32.png";
    private static final String FXML = "MainWindow.fxml";
    private static final int MIN_HEIGHT = 600;
    private static final int MIN_WIDTH = 450;

    private final Logger logger = LogsCenter.getLogger(this.getClass());

    private Stage primaryStage;
    private Logic logic;

    // Independent Ui parts residing in this Ui container
    private CalendarBoxPanel calandarBoxPanel;
    private PersonListPanel personListPanel;
    private EventListPanel eventListPanel;
    private Config config;
    private UserPrefs prefs;

    @FXML
    private StackPane browserPlaceholder;

    @FXML
    private Pane calendarPlaceholder;

    @FXML
    private StackPane commandBoxPlaceholder;

    @FXML
    private MenuItem helpMenuItem;

    @FXML
    private StackPane personListPanelPlaceholder;

    @FXML
    private StackPane eventListPanelPlaceholder;

    @FXML
    private StackPane personPanelPlaceholder;

    @FXML
    private StackPane resultDisplayPlaceholder;

    @FXML
    private StackPane statusbarPlaceholder;

    @FXML
    private TabPane tabPane;

    public MainWindow(Stage primaryStage, Config config, UserPrefs prefs, Logic logic) {
        super(FXML);

        // Set dependencies
        this.primaryStage = primaryStage;
        this.logic = logic;
        this.config = config;
        this.prefs = prefs;

        // Configure the UI
        setTitle(config.getAppTitle());
        setIcon(ICON);
        setWindowMinSize();
        setWindowDefaultSize(prefs);
        Scene scene = new Scene(getRoot());
        primaryStage.setScene(scene);

        setAccelerators();
        registerAsAnEventHandler(this);
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    private void setAccelerators() {
        setAccelerator(helpMenuItem, KeyCombination.valueOf("F1"));
    }

    /**
     * Sets the accelerator of a MenuItem.
     *
     * @param keyCombination the KeyCombination value of the accelerator
     */
    private void setAccelerator(MenuItem menuItem, KeyCombination keyCombination) {
        menuItem.setAccelerator(keyCombination);

        /*
         * TODO: the code below can be removed once the bug reported here
         * https://bugs.openjdk.java.net/browse/JDK-8131666
         * is fixed in later version of SDK.
         *
         * According to the bug report, TextInputControl (TextField, TextArea) will
         * consume function-key events. Because CommandBox contains a TextField, and
         * ResultDisplay contains a TextArea, thus some accelerators (e.g F1) will
         * not work when the focus is in them because the key event is consumed by
         * the TextInputControl(s).
         *
         * For now, we add following event filter to capture such key events and open
         * help window purposely so to support accelerators even when focus is
         * in CommandBox or ResultDisplay.
         */
        getRoot().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getTarget() instanceof TextInputControl && keyCombination.match(event)) {
                menuItem.getOnAction().handle(new ActionEvent());
                event.consume();
            }
        });
    }

    /**
     * Fills up all the placeholders of this window.
     */
    void fillInnerParts() {

        //@@author Haozhe321
        calandarBoxPanel = new CalendarBoxPanel(this.logic);
        calendarPlaceholder.getChildren().add(calandarBoxPanel.getRoot());
        //@@author

        personListPanel = new PersonListPanel(logic.getFilteredPersonList());
        personListPanelPlaceholder.getChildren().add(personListPanel.getRoot());

        eventListPanel = new EventListPanel(logic.getFilteredEventList());
        eventListPanelPlaceholder.getChildren().add(eventListPanel.getRoot());  //TO BE IMPLEMENTED

        //@@author shitian007
        PersonPanel personPanel = new PersonPanel(logic);
        personPanelPlaceholder.getChildren().add(personPanel.getRoot());
        //@@author

        ResultDisplay resultDisplay = new ResultDisplay();
        resultDisplayPlaceholder.getChildren().add(resultDisplay.getRoot());

        StatusBarFooter statusBarFooter = new StatusBarFooter(prefs.getResidentBookFilePath());
        statusbarPlaceholder.getChildren().add(statusBarFooter.getRoot());

        CommandBox commandBox = new CommandBox(logic);
        commandBoxPlaceholder.getChildren().add(commandBox.getRoot());
    }

    void hide() {
        primaryStage.hide();
    }

    private void setTitle(String appTitle) {
        primaryStage.setTitle(appTitle);
    }

    /**
     * Sets the given image as the icon of the main window.
     *
     * @param iconSource e.g. {@code "/images/help_icon.png"}
     */
    private void setIcon(String iconSource) {
        FxViewUtil.setStageIcon(primaryStage, iconSource);
    }

    /**
     * Sets the default size based on user preferences.
     */
    private void setWindowDefaultSize(UserPrefs prefs) {
        primaryStage.setHeight(prefs.getGuiSettings().getWindowHeight());
        primaryStage.setWidth(prefs.getGuiSettings().getWindowWidth());
        if (prefs.getGuiSettings().getWindowCoordinates() != null) {
            primaryStage.setX(prefs.getGuiSettings().getWindowCoordinates().getX());
            primaryStage.setY(prefs.getGuiSettings().getWindowCoordinates().getY());
        }
    }

    private void setWindowMinSize() {
        primaryStage.setMinHeight(MIN_HEIGHT);
        primaryStage.setMinWidth(MIN_WIDTH);
    }

    /**
     * Returns the current size and the position of the main Window.
     */
    GuiSettings getCurrentGuiSetting() {
        return new GuiSettings(primaryStage.getWidth(), primaryStage.getHeight(),
                (int) primaryStage.getX(), (int) primaryStage.getY());
    }

    /**
     * Opens the help window.
     */
    public void handleHelp() {
        HelpWindow helpWindow = new HelpWindow();
        helpWindow.show();
    }

    //@@author blackroxs
    /**
     * Handles import and allows user to choose file
     */
    @FXML
    public void handleImport() {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(null);
        String filePath = file.getAbsolutePath();
        System.out.println(filePath);

        if (file != null) {
            try {
                CommandResult commandResult = logic.execute(ImportCommand.COMMAND_WORD + " " + filePath);
                logger.info("Result: " + commandResult.feedbackToUser);
                raise(new NewResultAvailableEvent(commandResult.feedbackToUser));
            } catch (CommandException e) {
                logger.info("Invalid command: " + ImportCommand.MESSAGE_ERROR);
                raise(new NewResultAvailableEvent(e.getMessage()));
            } catch (ParseException e) {
                logger.info("Invalid command: " + ImportCommand.MESSAGE_ERROR);
                raise(new NewResultAvailableEvent(e.getMessage()));
            }
        }
    }

    //@@author
    void show() {
        primaryStage.show();
    }

    /**
     * Closes the application.
     */
    @FXML
    private void handleExit() {
        raise(new ExitAppRequestEvent());
    }

    public PersonListPanel getPersonListPanel() {
        return this.personListPanel;
    }

    void releaseResources() {
        calandarBoxPanel.freeResources();
    }

    @Subscribe
    private void handleShowHelpEvent(ShowHelpRequestEvent event) {
        logger.info(LogsCenter.getEventHandlingLogMessage(event));
        handleHelp();
    }

    //@@author Haozhe321
    @Subscribe
    public void handleCalenderBoxPanelChange(EventBookChangedEvent event) {
        switchTab(1);
        logger.info(LogsCenter.getEventHandlingLogMessage(event));
        calandarBoxPanel.getCalendarBox().refreshCalendar(this.logic);
    }

    @Subscribe
    public void handleChangeMonthCommand(ChangeMonthRequestEvent event) {
        logger.info(LogsCenter.getEventHandlingLogMessage(event));
        if (event.getTargetIndex() == 0) {
            calandarBoxPanel.getCalendarBox().previousMonth();
        } else if (event.getTargetIndex() == 1) {
            calandarBoxPanel.getCalendarBox().nextMonth();
        }
    }

    //@@author

    //@@author sushinoya
    public void switchTab(int index) {
        tabPane.getSelectionModel().select(index);
    }

    @Subscribe
    private void handleSwitchTabEvent(SwitchTabRequestEvent event) {
        logger.info(LogsCenter.getEventHandlingLogMessage(event));
        switchTab(event.targetIndex);
    }

    @Subscribe
    private void handlePersonPanelSelectionChange(PersonPanelSelectionChangedEvent event) {
        logger.info(LogsCenter.getEventHandlingLogMessage(event));
        switchTab(0);
    }

    @Subscribe
    public void handleResidentBoxPanelChange(ResidentBookChangedEvent event) {
        switchTab(0);
        logger.info(LogsCenter.getEventHandlingLogMessage(event));
    }
    //@@author

}
