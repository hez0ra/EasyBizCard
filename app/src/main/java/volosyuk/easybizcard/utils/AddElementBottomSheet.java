package volosyuk.easybizcard.utils;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import volosyuk.easybizcard.R;

public class AddElementBottomSheet extends BottomSheetDialogFragment {

    private OnElementSelectedListener listener;

    public interface OnElementSelectedListener {
        void onElementSelected(String elementType);
    }

    public void setOnElementSelectedListener(OnElementSelectedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_element_menu, container, false);

        view.findViewById(R.id.btn_add_image).setOnClickListener(v -> {
            if (listener != null) listener.onElementSelected("image");
            dismiss();
        });

        view.findViewById(R.id.btn_add_text).setOnClickListener(v -> {
            if (listener != null) listener.onElementSelected("text");
            dismiss();
        });

        view.findViewById(R.id.btn_add_phone).setOnClickListener(v -> {
            if (listener != null) listener.onElementSelected("phone");
            dismiss();
        });

        view.findViewById(R.id.btn_add_email).setOnClickListener(v -> {
            if (listener != null) listener.onElementSelected("email");
            dismiss();
        });

        view.findViewById(R.id.btn_add_link).setOnClickListener(v -> {
            if (listener != null) listener.onElementSelected("link");
            dismiss();
        });

        return view;
    }
}
