package com.bo.mocks;

import java.util.ArrayList;
import java.util.List;

import com.bo.keysandvalues.ErrorListener;

public class MockErrorListener implements ErrorListener
{
    private List<String> messages = new ArrayList<>();
    private List<Exception> errors = new ArrayList<>();



    @Override
    public void onError(String msg) {
        messages.add(msg);
    }

    /**
     * @return the errors
     */
    public List<Exception> getErrors() {
        return errors;
    }

    /**
     * @return the messages
     */
    public List<String> getMessages() {
        return messages;
    }

    @Override
    public void onError(String msg, Exception e) {
        messages.add(msg);
        errors.add(e);
    }

}