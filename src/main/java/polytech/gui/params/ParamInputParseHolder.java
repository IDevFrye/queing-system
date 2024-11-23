package polytech.gui.params;

import lombok.Data;

import javax.swing.JLabel;
import javax.swing.JTextField;

@Data
public class ParamInputParseHolder {
    private final JLabel jLabel;
    private final JTextField jTextField;
    public static ParamInputParseHolder of(JLabel label, JTextField textField) {
        return new ParamInputParseHolder(label, textField);
    }
}
