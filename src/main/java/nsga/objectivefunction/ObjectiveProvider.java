/*
 * MIT License
 *
 * Copyright (c) 2019 Debabrata Acharya
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package nsga.objectivefunction;

import nsga.plugin.FitnessCalculator;
import nsga.plugin.FitnessCalculatorProvider;

import java.util.ArrayList;
import java.util.List;

public class ObjectiveProvider {

	public static List<AbstractObjectiveFunction> provideSCHObjectives(int chromosomeLength) {
		return  ObjectiveProvider.provideSCHObjectives(
			FitnessCalculatorProvider.normalizedGeneticCodeValue(
				0,
				Math.pow(2, chromosomeLength) - 1,
				0,
				2
			)
		);
	}

	public static List<AbstractObjectiveFunction> provideSCHObjectives(FitnessCalculator fitnessCalculator) {

		List<AbstractObjectiveFunction> objectives = new ArrayList<>();

		objectives.add(new SCH_1(fitnessCalculator));
		objectives.add(new SCH_2(fitnessCalculator));

		return objectives;
	}

	public static List<AbstractObjectiveFunction> provideZDTObjectives() {

		List<AbstractObjectiveFunction> objectives = new ArrayList<>();

		objectives.add(new ZDT1_1());
		objectives.add(new ZDT1_2());

		return objectives;
	}

	/**
	 * 重构目标函数列表
	 * @return
	 */
	public static List<AbstractObjectiveFunction> provideRefactorObjectives() {
		return new ArrayList<AbstractObjectiveFunction>() {{
			add(new ConcernObjective());
			add(new SMQObjective());
		}};
	}

	/**
	 * 新基因型 - 重构函数列表
	 * @return
	 */
	public static List<AbstractObjectiveFunction> provideRefactorObjectivesEncode() {
		return new ArrayList<AbstractObjectiveFunction>() {{
			add(new ConcernNewObjective());
			add(new SMQNewObjective());
			add(new MoJoObjective());
		}};
	}

	/**
	 * 拆解SMQ为内聚和耦合两方面指标
	 * @return
	 */
	public static List<AbstractObjectiveFunction> provideRefactorObjectivesSplited() {
		return new ArrayList<AbstractObjectiveFunction>() {{
			add(new ConcernObjective());
			add(new CohesionObjective());
			add(new CouplingObjective());
		}};
	}
}
