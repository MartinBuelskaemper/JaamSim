/*
 * JaamSim Discrete Event Simulation
 * Copyright (C) 2014 Ausenco Engineering Canada Inc.
 * Copyright (C) 2016-2018 JaamSim Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jaamsim.Samples;

import com.jaamsim.basicsim.Entity;
import com.jaamsim.basicsim.ErrorException;
import com.jaamsim.basicsim.ObjectType;
import com.jaamsim.input.ExpError;
import com.jaamsim.input.ExpEvaluator;
import com.jaamsim.input.ExpParser;
import com.jaamsim.input.ExpResType;
import com.jaamsim.input.ExpResult;
import com.jaamsim.input.InputErrorException;
import com.jaamsim.units.Unit;
import com.jaamsim.units.UserSpecifiedUnit;

public class SampleExpression implements SampleProvider {
	private final ExpParser.Expression exp;
	private final Entity thisEnt;
	private final Class<? extends Unit> unitType;
	private final ExpEvaluator.EntityParseContext parseContext;

	public SampleExpression(String expString, Entity ent, Class<? extends Unit> ut) throws ExpError {

		// Check that a unit type has been specified
		if (ut == UserSpecifiedUnit.class) {
			throw new InputErrorException("Unit type has not been specified");
		}

		thisEnt = ent;
		unitType = ut;
		parseContext = ExpEvaluator.getParseContext(thisEnt, expString);
		exp = ExpParser.parseExpression(parseContext, expString);
		ExpParser.assertUnitType(exp, unitType);
		ExpParser.assertResultType(exp, ExpResType.NUMBER);
	}

	@Override
	public Class<? extends Unit> getUnitType() {
		return unitType;
	}

	@Override
	public double getNextSample(double simTime) {
		double ret = 0.0;
		try {
			ExpResult res = ExpEvaluator.evaluateExpression(exp, simTime);
			if (res.unitType != unitType)
				thisEnt.error("Invalid unit returned by an expression: '%s'%n"
						+ "Received: %s, expected: %s",
						exp, ObjectType.getObjectTypeForClass(res.unitType),
						ObjectType.getObjectTypeForClass(unitType));

			ret = res.value;
		}
		catch(ExpError e) {
			throw new ErrorException(thisEnt, e);
		}
		return ret;
	}

	@Override
	public double getMeanValue(double simTime) {
		return getNextSample(simTime);
	}

	@Override
	public double getMinValue() {
		return getNextSample(0.0d);
	}

	@Override
	public double getMaxValue() {
		return getNextSample(0.0d);
	}

	public String getExpressionString() {
		return parseContext.getUpdatedSource();
	}

	@Override
	public String toString() {
		return getExpressionString();
	}

}
