package com.f3cinema.app.ui.staff.ticketing;

import com.f3cinema.app.ui.staff.ticketing.components.ModernStepper;
import com.f3cinema.app.ui.staff.ticketing.step1.ShowtimeSelectionPanel;
import com.f3cinema.app.ui.staff.ticketing.step2.SeatSelectionPanel;
import com.f3cinema.app.ui.staff.ticketing.step3.SnacksSelectionPanel;
import com.f3cinema.app.ui.staff.ticketing.step4.PaymentPanel;

import javax.swing.*;
import java.awt.*;

/**
 * Main container for the unified 4-step ticketing flow.
 * Uses CardLayout to switch between steps seamlessly.
 */
public class TicketingFlowPanel extends JPanel {

    private static final String STEP_1 = "STEP_1_SHOWTIME";
    private static final String STEP_2 = "STEP_2_SEATS";
    private static final String STEP_3 = "STEP_3_SNACKS";
    private static final String STEP_4 = "STEP_4_PAYMENT";

    private final CardLayout cardLayout;
    private final JPanel cardContainer;
    private final TicketOrderState state;
    private ModernStepper stepper;

    private ShowtimeSelectionPanel step1;
    private SeatSelectionPanel step2;
    private SnacksSelectionPanel step3;
    private PaymentPanel step4;

    public TicketingFlowPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);

        state = TicketOrderState.getInstance();
        cardLayout = new CardLayout();
        cardContainer = new JPanel(cardLayout);
        cardContainer.setOpaque(false);

        initSteps();
        stepper = new ModernStepper("Suat chieu", "Ghe", "Bap nuoc", "Thanh toan");
        add(stepper, BorderLayout.NORTH);
        add(cardContainer, BorderLayout.CENTER);

        state.setCurrentStep(1);
        showStep(1);
    }

    private void initSteps() {
        step1 = new ShowtimeSelectionPanel(this);
        step2 = new SeatSelectionPanel(this);
        step3 = new SnacksSelectionPanel(this);
        step4 = new PaymentPanel(this);

        cardContainer.add(step1, STEP_1);
        cardContainer.add(step2, STEP_2);
        cardContainer.add(step3, STEP_3);
        cardContainer.add(step4, STEP_4);
    }

    public void nextStep() {
        int current = state.getCurrentStep();
        if (current < 4) {
            showStep(current + 1);
        }
    }

    public void previousStep() {
        int current = state.getCurrentStep();
        if (current > 1) {
            showStep(current - 1);
        }
    }

    private void showStep(int step) {
        state.setCurrentStep(step);
        switch (step) {
            case 1 -> cardLayout.show(cardContainer, STEP_1);
            case 2 -> {
                cardLayout.show(cardContainer, STEP_2);
                step2.onStepActivated();
            }
            case 3 -> {
                cardLayout.show(cardContainer, STEP_3);
                step3.onStepActivated();
            }
            case 4 -> {
                cardLayout.show(cardContainer, STEP_4);
                step4.onStepActivated();
            }
        }
        refreshStepper();
    }

    public void reset() {
        state.reset();
        showStep(1);
    }

    private void refreshStepper() {
        if (stepper != null) stepper.setCurrentStep(state.getCurrentStep());
    }
}
