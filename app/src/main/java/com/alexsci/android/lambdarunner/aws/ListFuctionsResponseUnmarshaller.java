package com.alexsci.android.lambdarunner.aws;

import com.amazonaws.transform.JsonUnmarshallerContext;
import com.amazonaws.transform.ListUnmarshaller;
import com.amazonaws.transform.SimpleTypeJsonUnmarshallers;
import com.amazonaws.transform.Unmarshaller;
import com.amazonaws.util.json.AwsJsonReader;

public class ListFuctionsResponseUnmarshaller implements
        Unmarshaller<ListFunctionsResponse, JsonUnmarshallerContext> {

    public ListFunctionsResponse unmarshall(JsonUnmarshallerContext context) throws Exception {
        ListFunctionsResponse listFunctionsResult = new ListFunctionsResponse();

        AwsJsonReader reader = context.getReader();
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("Functions")) {
                listFunctionsResult.setFunctions(new ListUnmarshaller<ListFunctionsResponseFunctions>(
                        ListFunctionsResponseFunctionUnmarshaller.getInstance()
                ).unmarshall(context));
            } else if (name.equals("NextMarker")) {
                listFunctionsResult.setNextMarker(SimpleTypeJsonUnmarshallers.StringJsonUnmarshaller.getInstance()
                        .unmarshall(context));
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();

        return listFunctionsResult;
    }

    private static ListFuctionsResponseUnmarshaller instance;

    public static ListFuctionsResponseUnmarshaller getInstance() {
        if (instance == null) {
            instance = new ListFuctionsResponseUnmarshaller();
        }
        return instance;
    }
}
