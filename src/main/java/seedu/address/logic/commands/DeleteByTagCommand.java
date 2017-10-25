package seedu.address.logic.commands;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.person.ReadOnlyPerson;
import seedu.address.model.tag.Tag;

import java.util.List;

public class DeleteByTagCommand extends UndoableCommand {
    public static final String COMMAND_WORD = "deletebytag";
    public static final String COMMAND_ALIAS = "dbt";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Deletes the persons identified by the tag supplied in this argument\n"
            + "Parameters: TAG\n"
            + "Example: " + COMMAND_WORD + "friends";

    public static final String MESSAGE_DELETE_PERSON_SUCCESS = "Deleted Persons with the following tag: %1$s";

    private final Tag toRemove;

    public DeleteByTagCommand(String tagName) throws IllegalValueException {
        this.toRemove = new Tag(tagName);
    }


    @Override
    public CommandResult executeUndoableCommand() throws CommandException {

        List<ReadOnlyPerson> lastShownList = model.getFilteredPersonList();


//        ReadOnlyPerson personToDelete = lastShownList.get(targetIndex.getZeroBased());

        try {
            model.deleteByTag(toRemove);
        } catch (IllegalValueException e ) {
            assert false : "Tag provided must be valid";
        }

        return new CommandResult(String.format(MESSAGE_DELETE_PERSON_SUCCESS, toRemove));
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof DeleteByTagCommand // instanceof handles nulls
                && this.toRemove.equals(((DeleteByTagCommand) other).toRemove)); // state check
    }
}


