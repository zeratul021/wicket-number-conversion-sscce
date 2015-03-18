package com.github.zeratul021.wicketnumberconversion;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.validation.validator.RangeValidator;

import java.math.BigDecimal;
import java.math.BigInteger;

public class HomePage extends WebPage {
    private static final long serialVersionUID = 1L;

    private BigDecimal bigDecimalInput;
    private BigInteger bigIntegerInput;
    private Long longInput;
    private Long validatedLongInput;
    private Long validatedM1LongInput;

    public HomePage(final PageParameters parameters) {
        super(parameters);

        add(new Label("version", getApplication().getFrameworkSettings().getVersion()));

        Form<Void> form = new Form<>("form");
        add(form);

        form.add(new Label("doubleMaxValue", BigDecimal.valueOf(Double.MAX_VALUE).toPlainString()));
        form.add(new Label("longMaxValue-1", Long.MAX_VALUE - 1));
        form.add(new Label("longMaxValue", Long.MAX_VALUE));
        form.add(new Label("longMaxValueInDouble", BigDecimal.valueOf((double) Long.MAX_VALUE).toPlainString()));
        form.add(new Label("longMaxValueInDoubleOver", BigDecimal.valueOf(((double) Long.MAX_VALUE)).add(BigDecimal.ONE).toPlainString()));
        IModel<BigInteger> bigIntegerModel = new PropertyModel<>(this, "bigIntegerInput");
        form.add(new Label("bigIntegerInputValue", bigIntegerModel));
        form.add(new TextField<>("bigIntegerInputSubmitted", bigIntegerModel));
        IModel<BigDecimal> bigDecimalModel = new PropertyModel<>(this, "bigDecimalInput");
        form.add(new Label("bigDecimalInputValue", bigDecimalModel));
        form.add(new TextField<>("bigDecimalInputSubmitted", bigDecimalModel));
        IModel<Long> longModel = new PropertyModel<>(this, "longInput");
        form.add(new Label("longInputValue", longModel));
        form.add(new TextField<>("longInputSubmitted", longModel));
        IModel<Long> validatedLongModel = new PropertyModel<>(this, "validatedLongInput");
        form.add(new Label("validatedLongInputValue", validatedLongModel));
        // The value of 'validatedLongInputSubmitted' is not a valid Long.
        // -> real threshold is actually 9223372036854776833 which is represented in double greater than double holding Long.MAX_VALUE (with precision loss)
        form.add(new TextField<>("validatedLongInputSubmitted", validatedLongModel).add(RangeValidator.maximum(Long.MAX_VALUE)));

        IModel<Long> validatedM1LongModel = new PropertyModel<>(this, "validatedM1LongInput");
        form.add(new Label("validatedM1LongInputValue", validatedM1LongModel));
        form.add(new TextField<>("validatedM1LongInputSubmitted", validatedM1LongModel).add(RangeValidator.maximum(Long.MAX_VALUE - 1)));
        form.add(new Button("submit") {
            @Override
            public void onSubmit() {
                super.onSubmit();
                System.out.println("Storing to the DB Long: " + longInput);
                System.out.println("Storing to the DB validated Long: " + validatedLongInput);

                // personal observations of double conversion

                // toString of java.text.DigitList 0.9223372036854775809x10^19
                char[] digits = "9223372036854775809".toCharArray();
                int count = 19;
                int decimalAt = 19;
                StringBuffer buf = new StringBuffer(19);
                buf.append("0.");
                buf.append(digits, 0, count);
                buf.append("x10^");
                buf.append(decimalAt);
                System.out.println("9223372036854775809 as DigitList.toString string: " + buf.toString());
                try {
                    System.out.println("Parse previous as double: " + Double.parseDouble(buf.toString()));
                } catch (NumberFormatException e) {
                    // this happens when step-through the DigitList#getDouble in the debugger
                    // same thing happens inside #getLong while debugging
                    System.out.println("Can not parse that string as double: " + e.getMessage());
                }
                // getDouble of java.text.DigitList
                buf = new StringBuffer(19);
                buf.append('.');
                buf.append(digits, 0, count);
                buf.append('E');
                buf.append(decimalAt);
                System.out.println("9223372036854775809 as string in DigitList.getDouble: " + buf.toString());
                try {
                    // this happens when I do not enter DigitList#getDouble in the debugger
                    System.out.println("Parse previous as double: " + Double.parseDouble(buf.toString()));
                } catch (NumberFormatException e) {
                    System.out.println("Can not parse that string as double: " + e.getMessage());
                }
            }
        });
        // 9223372036854775808 -> 9.223372036854776E18
        // .9223372036854775809E19 -> pass
        // 0.9223372036854775809x10^19 -> NFE
        add(new FeedbackPanel("feedback-panel"));
    }
}
