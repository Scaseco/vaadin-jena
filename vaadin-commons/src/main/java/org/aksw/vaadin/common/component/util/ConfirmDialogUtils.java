package org.aksw.vaadin.common.component.util;

import java.util.function.Consumer;

import org.claspina.confirmdialog.ButtonOption;
import org.claspina.confirmdialog.ConfirmDialog;

import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;

public class ConfirmDialogUtils {
    /**
     * Create a static confirmDialog with a preconfigured immutable text message.
     * Signature compatible with ConfirmDialog of vaadin pro.
     */
    public static ConfirmDialog confirmDialog(String header, String text, String confirmText,
            Consumer<?> confirmListener,
            String cancelText,
            Consumer<?> cancelListener) {

        TextArea textArea = new TextArea();
        textArea.setValue(text);
        textArea.setReadOnly(true);
        // textArea.setEnabled(false);

        ConfirmDialog result = ConfirmDialog
            .create()
            .withCaption(header)
            .withMessage(textArea)
            .withOkButton(() -> confirmListener.accept(null), ButtonOption.focus(), ButtonOption.caption(confirmText), ButtonOption.closeOnClick(true))
            .withCancelButton(() -> { if (cancelListener != null) { cancelListener.accept(null); } }, ButtonOption.caption(cancelText), ButtonOption.closeOnClick(true));

        return result;
    }

    public static ConfirmDialog confirmInputDialog(String header, String initialText, String confirmText,
            Consumer<String> confirmListener,
            String cancelText,
            Consumer<?> cancelListener) {

        TextField textField = new TextField();
        if (initialText != null) {
            textField.setValue(initialText);
        }
        textField.focus();
        // textArea.setReadOnly(true);
        // textArea.setEnabled(false);

        ConfirmDialog result = ConfirmDialog
            .create()
            .withCaption(header)
            .withMessage(textField)
            .withOkButton(() -> confirmListener.accept(textField.getValue()), ButtonOption.caption(confirmText), ButtonOption.closeOnClick(true))
            .withCancelButton(() -> cancelListener.accept(null), ButtonOption.caption(cancelText), ButtonOption.closeOnClick(true));

        return result;
    }

}
