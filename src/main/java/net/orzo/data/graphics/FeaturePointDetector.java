/*
 * Copyright (C) 2010 Tomas Machalek
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.orzo.data.graphics;

import java.util.List;

/**
 * Abstraction of object providing feature point detection service.
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 */
public interface FeaturePointDetector {

	/**
	 * Searches for image's feature points using some feature point detection
	 * algorithm.
	 * 
	 * @return list of corner points
	 */
	public FeaturePointDetector analyze(int threshold);

	/**
	 * Searches for image's feature points with default threshold.
	 */
	public FeaturePointDetector analyze();

	/**
	 * Fetches feature points with specified minimum distance between strongest
	 * ones.
	 */
	public List<CornerPoint> fetchPoints(int dmin);

}
