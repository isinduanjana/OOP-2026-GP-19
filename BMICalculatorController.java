import javax.swing.*;
import java.awt.event.*;

public class BMICalculatorController implements ActionListener {
    private JTextField weightField;
    private JTextField heightField;
    private JRadioButton englishBtn;
    private JRadioButton metricBtn;
    private JLabel resultLabel;

    public BMICalculatorController(JTextField weightField, JTextField heightField,
                                   JRadioButton englishBtn, JRadioButton metricBtn,
                                   JLabel resultLabel) {
        this.weightField = weightField;
        this.heightField = heightField;
        this.englishBtn = englishBtn;
        this.metricBtn = metricBtn;
        this.resultLabel = resultLabel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            double weight = Double.parseDouble(weightField.getText());
            double height = Double.parseDouble(heightField.getText());

            if (weight <= 0 || height <= 0) {
                JOptionPane.showMessageDialog(null, "Please enter a valid weight and a valid height");
                return;
            }

            int unit = metricBtn.isSelected() ? UnitSystem.METRIC : UnitSystem.ENGLISH;

            BMICalculator calc = new BMICalculator(weight, height, unit);
            double bmi = calc.calculateBMI();
            String category = calc.getCategory(bmi);

            resultLabel.setText(String.format("BMI = %.2f (%s)", bmi, category));

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Please enter a valid weight and a valid height");
        }
    }
}
