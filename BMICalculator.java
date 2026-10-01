public class BMICalculator {
    private double weight;
    private double height;
    private int unit;

    public BMICalculator(double weight, double height, int unit) {
        this.weight = weight;
        this.height = height;
        this.unit = unit;
    }
    public double calculateBMI() {
        if (unit == UnitSystem.METRIC) {
            return weight / (height * height);
        }else {
            return (weight * 703)/(height * height);
        }
    }

    public String getCategory(double bmi){
        if(bmi < 18.5){
            return "Underweight";
        }
        else if(bmi < 25){
            return "Normal";
        }
        else if(bmi < 30){
            return "Overweight";
        }else{
            return "Obese";
        }
    }
}
