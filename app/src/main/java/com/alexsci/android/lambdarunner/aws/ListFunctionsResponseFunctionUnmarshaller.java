package com.alexsci.android.lambdarunner.aws;

import com.amazonaws.transform.JsonUnmarshallerContext;
import com.amazonaws.transform.ListUnmarshaller;
import com.amazonaws.transform.SimpleTypeJsonUnmarshallers;
import com.amazonaws.transform.Unmarshaller;
import com.amazonaws.util.json.AwsJsonReader;

public class ListFunctionsResponseFunctionUnmarshaller implements
        Unmarshaller<ListFunctionsResponseFunctions, JsonUnmarshallerContext> {

    public ListFunctionsResponseFunctions unmarshall(JsonUnmarshallerContext context) throws Exception {
        AwsJsonReader reader = context.getReader();
        if (!reader.isContainer()) {
            reader.skipValue();
            return null;
        }
        ListFunctionsResponseFunctions identityDescription = new ListFunctionsResponseFunctions();
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("Description")) {
                identityDescription.setDescription(SimpleTypeJsonUnmarshallers.StringJsonUnmarshaller.getInstance()
                        .unmarshall(context));
            } else if (name.equals("FunctionName")) {
                identityDescription.setFunctionName(SimpleTypeJsonUnmarshallers.StringJsonUnmarshaller.getInstance()
                        .unmarshall(context));
            } else if (name.equals("FunctionArn")) {
                identityDescription.setFunctionArn(SimpleTypeJsonUnmarshallers.StringJsonUnmarshaller.getInstance()
                        .unmarshall(context));
            } else if (name.equals("LastModified")) {
                identityDescription.setLastModified(SimpleTypeJsonUnmarshallers.StringJsonUnmarshaller.getInstance()
                        .unmarshall(context));
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return identityDescription;
    }

    private static ListFunctionsResponseFunctionUnmarshaller instance;

    public static ListFunctionsResponseFunctionUnmarshaller getInstance() {
        if (instance == null)
            instance = new ListFunctionsResponseFunctionUnmarshaller();
        return instance;
    }
}
