package polytech.gui.params;

import polytech.gui.main.MassServiceSystemFrame;
import polytech.system.MassServiceSystemParams;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class ParamsInputFrame extends JFrame {
    private static final int WIDTH_PX = 750;
    private static final int HEIGHT_PX = 550;
    private final JButton paramsSubmitButton = new JButton("Ввести параметры!");
    public static final NavigableMap<Integer, ParamInputParseHolder> paramsInputLabelsAndFields =
            new TreeMap<>(Integer::compareTo);
    {
        paramsInputLabelsAndFields.putAll(Map.of(
                1, ParamInputParseHolder.of(new JLabel("Количество генераторов"), new JTextField("3")),
                2, ParamInputParseHolder.of(new JLabel("Размер буфера"), new JTextField("3")),
                3, ParamInputParseHolder.of(new JLabel("Количество обработчиков"), new JTextField("3")),
                4, ParamInputParseHolder.of(new JLabel("Параметр распределения"), new JTextField("0.2")),
                5, ParamInputParseHolder.of(new JLabel("Макс. время работы"), new JTextField("1000")),
                6, ParamInputParseHolder.of(new JLabel("Мин. время обработки"), new JTextField("5")),
                7, ParamInputParseHolder.of(new JLabel("Макс. время обработки"), new JTextField("6"))
        ));
    }

    public ParamsInputFrame() {
        setTitle("Введите параметры для работы СМО");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(WIDTH_PX, HEIGHT_PX);
        setResizable(false);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        contentPanel.setBackground(Color.decode("#F7F9FC"));
        add(contentPanel);

        JLabel titleLabel = new JLabel("Параметры системы массового обслуживания");
        titleLabel.setFont(loadCustomFont("Montserrat-SemiBold.ttf", 20));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(new EmptyBorder(10, 0, 20, 0));
        contentPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel paramsPanel = new JPanel();
        paramsPanel.setLayout(new BoxLayout(paramsPanel, BoxLayout.Y_AXIS));
        paramsPanel.setOpaque(false);

        for (var input : paramsInputLabelsAndFields.entrySet()) {
            paramsPanel.add(createRoundedParamPanel(
                    input.getValue().getJLabel().getText(),
                    input.getValue().getJTextField()
            ));
        }
        JScrollPane scrollPane = new JScrollPane(paramsPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.decode("#F7F9FC"));

        paramsSubmitButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                paramsSubmitButton.setBackground(new Color(60, 60, 60));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                paramsSubmitButton.setBackground(new Color(0, 0, 0));
            }

            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                paramsSubmitButton.setBackground(new Color(40, 40, 40));
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                paramsSubmitButton.setBackground(new Color(60, 60, 60));
            }
        });
        paramsSubmitButton.setFont(loadCustomFont("Montserrat-SemiBold.ttf", 16));
        paramsSubmitButton.setPreferredSize(new Dimension(200, 40));
        paramsSubmitButton.setBackground(new Color(0, 0, 0));
        paramsSubmitButton.setForeground(Color.WHITE);
        paramsSubmitButton.setFocusPainted(false);
        paramsSubmitButton.setBorder(BorderFactory.createLineBorder(Color.decode("#4A90E2")));
        paramsSubmitButton.addActionListener(this::submitParams);
        buttonPanel.add(paramsSubmitButton);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        setVisible(true);
    }

    private JPanel createRoundedParamPanel(String labelText, JTextField inputField) {
        JPanel roundedPanel = new JPanel();
        roundedPanel.setLayout(new GridBagLayout());
        roundedPanel.setBackground(Color.WHITE);
        roundedPanel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true));
        roundedPanel.setPreferredSize(new Dimension(500, 50));

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 10, 5, 10);

        JLabel label = new JLabel(labelText);
        label.setFont(loadCustomFont("Montserrat-Regular.ttf", 14));
        label.setPreferredSize(new Dimension(250, 30));
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        roundedPanel.add(label, constraints);

        inputField.setFont(loadCustomFont("Montserrat-Regular.ttf", 14));
        inputField.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230), 1));
        inputField.setMargin(new Insets(5, 10, 5, 10));

        inputField.setPreferredSize(new Dimension(400, 30));
        constraints.gridx = 1;
        constraints.weightx = 1.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        roundedPanel.add(inputField, constraints);

        return roundedPanel;
    }

    private Font loadCustomFont(String fontFileName, float size) {
        try {
            return Font.createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream("/fonts/" + fontFileName))
                    .deriveFont(size);
        } catch (Exception e) {
            return new Font("Arial", Font.PLAIN, (int) size);
        }
    }

    public void submitParams(ActionEvent e) {
        dispose();
        new MassServiceSystemFrame(MassServiceSystemParams.builder()
                .withProducerCount(Integer.parseInt(paramsInputLabelsAndFields.get(1).getJTextField().getText()))
                .withBufferCapacity(Integer.parseInt(paramsInputLabelsAndFields.get(2).getJTextField().getText()))
                .withDeviceCount(Integer.parseInt(paramsInputLabelsAndFields.get(3).getJTextField().getText()))
                .withLambda(Double.parseDouble(paramsInputLabelsAndFields.get(4).getJTextField().getText()))
                .withMaxSimulationTime(Double.parseDouble(paramsInputLabelsAndFields.get(5).getJTextField().getText()))
                .withMinDeviceProcessingTime(Double.parseDouble(paramsInputLabelsAndFields.get(6).getJTextField().getText()))
                .withMaxDeviceProcessingTime(Double.parseDouble(paramsInputLabelsAndFields.get(7).getJTextField().getText()))
                .build());
    }
}
