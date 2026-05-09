package com.agrotech.api.shared.application.internal;

import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.multipart.MultipartFile;

import java.beans.PropertyEditorSupport;

@ControllerAdvice
public class MultipartFileInitBinder {

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(MultipartFile.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                // Cuando el formulario envía photo="" tratarlo como null para evitar el typeMismatch
                setValue(null);
            }
        });
    }
}