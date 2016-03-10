package com.cambiolabs.citewrite.data;

import java.util.Comparator;

public class PermitColumnMetaComp implements Comparator<PermitColumnMetaData>
{

	@Override
	public int compare(PermitColumnMetaData arg0, PermitColumnMetaData arg1) {
		if(arg0.displayOrder > arg1.displayOrder){
            return 1;
        } else {
            return -1;
        }
	}
	
}
