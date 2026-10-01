import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class BMICalculatorApp extends JFrame {
    private JTextField weightField;
    private JTextField heightField;
    private JRadioButton englishBtn;
    private JRadioButton metricBtn;
    private JLabel resultLabel;
    private JButton calculateBtn;


    public BMICalculatorApp() {
        setTitle("BMICalculator");
        setSize(400, 250);


        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(6,2,5,5));

        add(new JLabel("Weight:"));
        weightField = new JTextField();
        add(weightField);

        add(new JLabel("Height:"));
        heightField = new JTextField();
        add(heightField);

        add(new JLabel("UNIT :"));
        JPanel unitPanel = new JPanel();

        englishBtn = new JRadioButton("English(lb,in)");
        metricBtn = new JRadioButton("Metric(kg,m)");
        ButtonGroup bg = new ButtonGroup();
        bg.add(englishBtn);
        bg.add(metricBtn);

        englishBtn.setSelected(true);
        unitPanel.add(englishBtn);
        unitPanel.add(metricBtn);
        add(unitPanel);

        add(new JLabel(""));
        calculateBtn = new JButton("Calculate BMI");
        add(calculateBtn);

        add(new JLabel("Result :"));
        resultLabel = new JLabel("");
        add(resultLabel);

        //connect controller

        BMICalculatorController controller = new BMICalculatorController(
                weightField,
                heightField,
                englishBtn,
                metricBtn,
                resultLabel
        );
        calculateBtn.addActionListener(controller);
        setVisible(true);
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(()
        -> new BMICalculatorApp());
    }
}
