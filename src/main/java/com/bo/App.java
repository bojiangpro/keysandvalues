package com.bo;

import java.util.Arrays;
import java.util.Scanner;

import com.bo.context.Context;
import com.bo.context.ContextImpl;
import com.bo.keysandvalues.ErrorListener;
import com.bo.keysandvalues.KeysAndValues;
import com.bo.keysandvalues.KeysAndValuesImpl;
import com.bo.keysandvalues.dataprocessing.*;
import com.bo.keysandvalues.job.TransactionExtractor;
import com.bo.keysandvalues.job.JobExtractor;

public class App 
{
    public static void main( String[] args )
    {
        KeysAndValues keysAndValues = init();
        Scanner scanner = new Scanner(System.in);
        while(true)
        {
            try 
            {
                String kvPairs = scanner.nextLine();
                keysAndValues.accept(kvPairs);
                System.out.println(keysAndValues.display());
            }
            catch(Exception e)
            {
                scanner.close();
                break;
            }
        }
    }

	private static KeysAndValues init() {
		Context context = new ContextImpl();
        ErrorListener errorListener = new ErrorListener(){
        
            @Override
            public void onError(String msg, Exception e) {
                System.out.println(msg);
                System.out.println(e.getMessage());
            }
        
            @Override
            public void onError(String msg) {
                System.out.println(msg);
            }
        };
        context.Register(ErrorListener.class, errorListener);
        context.Register(Parser.class, new CsvParser());
        context.Register(Formatter.class, new OrderedLineFormatter());
        TransactionExtractor transaction = new TransactionExtractor(context);
        transaction.addAtomicGroup(Arrays.asList("441", "442", "500"));
        context.Register(JobExtractor.class, transaction);
        return new KeysAndValuesImpl(context);
    }
}
