import javax.swing.*;
import java.awt.event.*;

public class BMICalculatorController implements ActionListener {
    private JTextField weightField;
    private JTextField heightField;
    private JRadioButton englishBtn;
    private JRadioButton metricBtn;
    private JLabel resultLabel;

    public BMICalculatorController(JRadioButton englishBtn, JTextField heightField, JRadioButton metricBtn, JLabel resultLabel, JTextField weightField) {
        this.englishBtn = englishBtn;
        this.heightField = heightField;
        this.metricBtn = metricBtn;
        this.resultLabel = resultLabel;
        this.weightField = weightField;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        double weight = Double.parseDouble(weightField.getText());
        double height = Double.parseDouble(heightField.getText());
        if (weight <= 0 || height <= 0) {
            JOptionPane.showMessageDialog(null, "Please enter a valid weight and a valid height");
            return;
        }
        int unit = metricBtn.isSelected() ? UnitSystem.METRIC : UnitSystem.English;
        BMICalculator calc = new BMICalculator(weight, height, unit);
        double bmi = calc.calculateBMI();
        String category = calc.getCategory(bmi);

        resultLabel.setText(String.format("BMI=%.2f-%s", bmi, category));

    catch(NumberFormatException ex){
            JOptionPane.showMessageDialog(null, "Please enter a valid weight and a valid height");
        }
    }
}
