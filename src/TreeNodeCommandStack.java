/**
 * Copyright(c) 2012-2013 
 * @author tmoskun, ezcode (Zlatco)
 * This Software is distributed under MIT license
 */

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;

public class TreeNodeCommandStack {

	private final List<Command> commands = new ArrayList<Command>();
	private int currentLocation = -1;
	private int saveLocation = currentLocation;
	private AbstractButton _undo = null;
	private AbstractButton _redo = null;
	
	public void registerButtons(AbstractButton undo, AbstractButton redo) {
		_undo = undo;
		_redo = redo;
	}
	
	private void enableButtons(boolean enableUndo, boolean enableRedo) {
		if(_undo != null)
			_undo.setEnabled(enableUndo);
		if(_redo != null)
			_redo.setEnabled(enableRedo);
	}

	public void add(Command command) {
		clearInFrontOfCurrent();
		command.execute();
		commands.add(command);
		currentLocation++;
		enableButtons(true, false);
	}

	public void undo() {
		if(undoEnabled()) {
			((Command) commands.get(currentLocation)).undo();
			currentLocation--;
			enableButtons(undoEnabled(), true);
		}
	}

	public boolean undoEnabled() {
		return currentLocation >= 0;
	}

	public void redo() {
		currentLocation++;
		enableButtons(true, redoEnabled());
		((Command) commands.get(currentLocation)).execute();
	}

	public boolean redoEnabled() {
		return currentLocation < commands.size() - 1;
	}

	public boolean dirty() {
		return currentLocation != saveLocation;
	}

	private void clearInFrontOfCurrent() {
		while (currentLocation < commands.size() - 1) {
			commands.remove(currentLocation + 1);
		}
	}

	public void markSaveLocation() {
		saveLocation = currentLocation;
	}

	public String toString() {
		return commands.toString();
	}
	
		
	public interface Command {
	
		public void execute();

		public void undo();	
		
	}
	
	

}
