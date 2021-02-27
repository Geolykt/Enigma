package cuchaz.enigma.gui.elements;

import java.awt.GridLayout;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.Document;

import cuchaz.enigma.gui.events.ConvertingTextFieldListener;
import cuchaz.enigma.gui.events.ConvertingTextFieldListener.StopEditingCause;
import cuchaz.enigma.gui.util.GuiUtil;
import cuchaz.enigma.utils.validation.ParameterizedMessage;
import cuchaz.enigma.utils.validation.Validatable;

/**
 * A label that converts into an editable text field when you click it.
 */
public class ConvertingTextField implements Validatable {

	private final JPanel ui;
	private final ValidatableTextField textField;
	private final JLabel label;
	private boolean isEditing = false;

	private final Set<ConvertingTextFieldListener> listeners = new HashSet<>();

	public ConvertingTextField(String text) {
		this.ui = new JPanel();
		this.ui.setLayout(new GridLayout(1, 1, 0, 0));
		this.textField = new ValidatableTextField(text);
		this.textField.setFocusTraversalKeysEnabled(false);
		this.label = GuiUtil.unboldLabel(new JLabel(text));
		this.label.setBorder(BorderFactory.createLoweredBevelBorder());

		this.label.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				startEditing();
			}
		});

		this.textField.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				if (!hasChanges()) {
					stopEditing(StopEditingCause.ABORT);
				}
			}
		});

		this.textField.addKeyListener(new KeyAdapter() {
			
			@Override
			public void keyPressed(KeyEvent e) {
				switch (e.getKeyCode()) {
				case KeyEvent.VK_ESCAPE:
					stopEditing(StopEditingCause.ABORT);
					break;
				case KeyEvent.VK_ENTER:
					stopEditing(StopEditingCause.DO);
					break;
				case KeyEvent.VK_TAB:
					stopEditing(StopEditingCause.TAB);
					break;
				default:
					break;
				}
			}
		});

		this.ui.add(this.label);
	}

	public void startEditing() {
		if (isEditing) return;
		this.ui.removeAll();
		this.ui.add(this.textField);
		this.isEditing = true;
		this.ui.validate();
		this.ui.repaint();
		this.textField.requestFocusInWindow();
		this.textField.selectAll();
		this.listeners.forEach(l -> l.onStartEditing(this));
	}

	public void stopEditing(ConvertingTextFieldListener.StopEditingCause cause) {
		if (!isEditing) return;

		if (!listeners.stream().allMatch(l -> l.tryStopEditing(this, cause))) return;

		if (cause == StopEditingCause.ABORT) {
			this.textField.setText(this.label.getText());
		} else {
			this.label.setText(this.textField.getText());
		}

		this.ui.removeAll();
		this.ui.add(this.label);
		this.isEditing = false;
		this.ui.validate();
		this.ui.repaint();
		this.listeners.forEach(l -> l.onStopEditing(this, cause));
	}

	public void setText(String text) {
		stopEditing(StopEditingCause.ABORT);
		this.label.setText(text);
		this.textField.setText(text);
	}

	public void setEditText(String text) {
		if (!isEditing) return;

		this.textField.setText(text);
	}

	public void selectAll() {
		if (!isEditing) return;

		this.textField.selectAll();
	}

	public void selectSubstring(int startIndex) {
		if (!isEditing) return;

		Document doc = this.textField.getDocument();
		if (doc != null) {
			this.selectSubstring(startIndex, doc.getLength());
		}
	}

	public void selectSubstring(int startIndex, int endIndex) {
		if (!isEditing) return;

		this.textField.select(startIndex, endIndex);
	}

	public String getText() {
		if (isEditing) {
			return this.textField.getText();
		} else {
			return this.label.getText();
		}
	}

	public String getPersistentText() {
		return this.label.getText();
	}

	public boolean hasChanges() {
		if (!isEditing) return false;
		return !this.textField.getText().equals(this.label.getText());
	}

	@Override
	public void addMessage(ParameterizedMessage message) {
		textField.addMessage(message);
	}

	@Override
	public void clearMessages() {
		textField.clearMessages();
	}

	public void addListener(ConvertingTextFieldListener listener) {
		this.listeners.add(listener);
	}

	public void removeListener(ConvertingTextFieldListener listener) {
		this.listeners.remove(listener);
	}

	public JPanel getUi() {
		return ui;
	}

}
