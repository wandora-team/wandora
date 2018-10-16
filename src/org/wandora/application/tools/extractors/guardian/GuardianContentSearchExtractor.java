/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 *
 * Copyright (C) 2004-2016 Wandora Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.wandora.application.tools.extractors.guardian;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wandora.application.Wandora;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.utils.IObox;

/**
 *
 * @author Eero
 */

public class GuardianContentSearchExtractor extends AbstractGuardianExtractor {

	private static final long serialVersionUID = 1L;
	
	private static String defaultLang = "en";
	private static String currentURL = null;

	
	@Override
	public String getName() {
		return "The Guardian Content Search API extractor";
	}

	
	@Override
	public String getDescription() {
		return "Extractor performs an content search using The Guardian API and "
				+ "transforms results to topics and associations.";
	}

	
	// -------------------------------------------------------------------------

	
	@Override
	public boolean _extractTopicsFrom(File f, TopicMap tm) throws Exception {
		currentURL = null;
		String in = IObox.loadFile(f);
		JSONObject json = new JSONObject(in);
		parse(json, tm);
		return true;
	}

	@Override
	public boolean _extractTopicsFrom(URL u, TopicMap tm) throws Exception {
		currentURL = u.toExternalForm();

		log("Content search extraction with " + currentURL);

		String in = IObox.doUrl(u);

		System.out.println(
				    "The Guardian API returned-------------------------\n" + in
				+ "\n----------------------------------------------------");

		JSONObject json = new JSONObject(in);

		if (json.has("response")) {
			try {
				JSONObject response = json.getJSONObject("response");
				int nResults = response.getJSONArray("results").length();
				if (response.has("didYouMean") && nResults == 0) {
					String dym = response.getString("didYouMean");
					int didMean = WandoraOptionPane.showConfirmDialog(
							Wandora.getWandora(),
							"Did you mean \"" + dym + "\"", 
							"Did you mean", 
							WandoraOptionPane.YES_NO_OPTION);
					
					if (didMean == WandoraOptionPane.YES_OPTION) {
						URL newUrl = new URL(currentURL.replaceAll("&q=[^&]*", "&q=" + dym));
						System.out.println(newUrl.toString());
						this._extractTopicsFrom(newUrl, tm);
					} 
					else {
						parse(response, tm);
					}
				} 
				else {
					parse(response, tm);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}

		}
		return true;
	}
	
	

	@Override
	public boolean _extractTopicsFrom(String str, TopicMap tm) throws Exception {
		currentURL = null;
		JSONObject json = new JSONObject(str);
		if (json.has("response")) {
			JSONObject response = json.getJSONObject("response");
			parse(response, tm);
		}
		return true;
	}
	

	// -------------------------------------------------------------------------

	
	public void parse(JSONObject json, TopicMap tm) throws TopicMapException {
		if (json.has("results")) {
			try {
				JSONArray resultsArray = json.getJSONArray("results");
				for (int i = 0; i < resultsArray.length(); i++) {
					JSONObject result = resultsArray.getJSONObject(i);
					parseResult(result, tm);
				}
			} catch (JSONException ex) {
				System.out.println(ex);
				log(ex);
			}
		}
		handlePagination(json, tm);
	}

	private boolean shouldHandlePagination = true;
	private String defaultPagingOption = null;

	
	private void handlePagination(JSONObject json, TopicMap tm) {
		if (!shouldHandlePagination || forceStop())
			return;
		if (json.has("pages")) {
			try {
				int page = json.getInt("currentPage");
				int total = json.getInt("pages");
				if (page < total) {
					if (currentURL != null) {
						String[] pagingOptions = new String[] { "Do not extract any more pages",
								"Extract only next page", "Extract next page", "Extract 10 next pages",
								"Extract all next pages" };
						String message = "You have just extracted page " + page + ". There is total " + total
								+ " pages available. What would you like to do? "
								+ "Remember The Guardian APIs limit daily requests. Extracting one page takes one request.";
						if (defaultPagingOption == null)
							defaultPagingOption = pagingOptions[0];
						String a = WandoraOptionPane.showOptionDialog(Wandora.getWandora(), message, "Found more pages",
								WandoraOptionPane.OK_CANCEL_OPTION, pagingOptions, defaultPagingOption);
						defaultPagingOption = a;
						if (a != null) {
							String originalURL = currentURL;
							try {
								if (pagingOptions[1].equals(a)) {
									System.out.println("Selected to extract only next page");
									String newURL = originalURL.replace("page=" + page, "page=" + (page + 1));
									shouldHandlePagination = false;
									_extractTopicsFrom(new URL(newURL), tm);
								}

								else if (pagingOptions[2].equals(a)) {
									System.out.println("Selected to extract next page");
									String newURL = originalURL.replace("page=" + page, "page=" + (page + 1));
									_extractTopicsFrom(new URL(newURL), tm);
								}

								else if (pagingOptions[3].equals(a)) {
									System.out.println("Selected to extract 10 next pages");
									shouldHandlePagination = false;
									setProgress(1);
									setProgressMax(10);
									int progress = 1;
									for (int p = page + 1; p <= Math.min(page + 10, total) && !forceStop(); p++) {
										String newURL = originalURL.replace("page=" + page, "page=" + p);
										if (p == page + 10)
											shouldHandlePagination = true;
										_extractTopicsFrom(new URL(newURL), tm);
										setProgress(progress++);
										nap();
									}
								}

								else if (pagingOptions[4].equals(a)) {
									System.out.println("Selected to extract all pages");
									shouldHandlePagination = false;
									setProgress(1);
									setProgressMax((int) (total));
									int progress = 1;
									for (int p = page + 1; p <= total && !forceStop(); p++) {
										String newURL = originalURL.replace("page=" + page, "page=" + p);
										_extractTopicsFrom(new URL(newURL), tm);
										setProgress(progress++);
										nap();
									}
									shouldHandlePagination = true;
								}
							} 
							catch (Exception e) {
								log(e);
							}
						}
					}
				}
			} catch (JSONException ex) {
				log(ex);
			}
		}
	}

	
	
	private void nap() {
		try {
			Thread.sleep(200);
		} catch (Exception e) {
			// WAKE UP
		}
	}

	
	
	public void parseResult(JSONObject result, TopicMap tm) throws JSONException, TopicMapException {

		ArrayList<String> toAsso = new ArrayList<String>();
		toAsso.add("shouldHideAdverts");
		toAsso.add("commentable");
		toAsso.add("showInRelatedContent");
		toAsso.add("liveBloggingNow");
		toAsso.add("hasStoryPackage");
		toAsso.add("publication");

		if (result.has("id")) {
			String id = result.getString("id");
			Topic contentTopic = tm.createTopic();
			contentTopic.addSubjectIdentifier(new Locator(GUARDIAN_BASE_SI + id));
			contentTopic.addType(getContentType(tm));

			if (result.has("webTitle")) {
				Topic titleTypeTopic = getTitleType(tm);
				contentTopic.setBaseName(result.getString("webTitle"));
				contentTopic.setDisplayName("en", result.getString("webTitle"));
				parseOccurrence(result, "webTitle", tm, contentTopic, titleTypeTopic);
			}

			if (result.has("webPublicationDate")) {
				Topic pubDateTypeTopic = getPubTimeType(tm);
				parseOccurrence(result, "webPublicationDate", tm, contentTopic, pubDateTypeTopic);
				SimpleDateFormat dfin = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
				SimpleDateFormat dfout = new SimpleDateFormat("yyyy-MM-dd");
				try {
					Date d = dfin.parse(result.getString("webPublicationDate"));
					String shortDate = dfout.format(d);
					Association a = tm.createAssociation(getDateType(tm));
					Topic dateTopic = getDateTopic(tm, shortDate);
					a.addPlayer(contentTopic, getContentType(tm));
					a.addPlayer(dateTopic, getDateType(tm));
				} catch (Exception e) {
					log(e);
				}

			}

			if (result.has("fields")) {
				JSONObject fields = result.getJSONObject("fields");
				Iterator keys = fields.keys();
				while (keys.hasNext()) {
					String key = keys.next().toString();
					Topic type = getFieldType(tm, key);
					if (toAsso.contains(key)) {
						parseFieldAssociation(fields, key, tm, contentTopic, type);
					} else {
						parseOccurrence(fields, key, tm, contentTopic, type);
					}
				}
			}

			if (result.has("tags")) {
				JSONArray tags = result.getJSONArray("tags");
				for (int i = 0; i < tags.length(); i++) {

					JSONObject tag = tags.getJSONObject(i);
					String tagId = tag.getString("id");
					String tagUrl = tag.getString("webUrl");
					String tagTitle = tag.has("webTitle") ? tag.getString("webTitle") : null;
					String tagTtype = tag.has("type") ? tag.getString("type") : null;
					String tagSectId = tag.has("sectionId") ? tag.getString("sectionId") : null;
					String tagSectName = tag.has("sectionName") ? tag.getString("sectionName") : null;

					Topic tagTopic = getOrCreateTopic(tm, TAG_BASE_SI + tagId);
					Topic tagTopicType = getTagTopicType(tm);
					tagTopic.addType(tagTopicType);
					tagTopic.addSubjectIdentifier(new Locator(tagUrl));

					Association a = tm.createAssociation(tagTopicType);
					a.addPlayer(tagTopic, tagTopicType);
					a.addPlayer(contentTopic, getContentType(tm));

					tagTopic.setBaseName(tagId);
					String displayName = tagTitle == null ? tagId : tagTitle;
					tagTopic.setDisplayName(displayName, defaultLang);

					if (tagTtype != null) {
						parseTagAssociation(tag, "type", tm, tagTopic, tagTopicType);
					}

					if (tagSectId != null) {
						parseTagAssociation(tag, "sectionId", tm, tagTopic, tagTopicType);
					}

					if (tagSectName != null) {
						parseTagAssociation(tag, "sectionName", tm, tagTopic, tagTopicType);
					}
				}
			}
		}
	}
	
	

	private void parseOccurrence(JSONObject result, String jsonObjectName, TopicMap tm, Topic t, Topic ty) {
		try {
			Topic langTopic = getLangTopic(tm);
			String s = result.getString(jsonObjectName);
			if (s != null && s.length() > 0) {
				t.setData(ty, langTopic, s);
			}
		} catch (Exception ex) {
			log(ex);
		}
	}

	
	private void parseFieldAssociation(JSONObject result, String jsonObjectName, TopicMap tm, Topic ct, Topic ty) {
		try {
			String s = result.getString(jsonObjectName);
			if (s != null && s.length() > 0) {
				Topic t = getFieldTopic(tm, jsonObjectName, s);
				Topic cty = getContentType(tm);
				Association a = tm.createAssociation(ty);
				a.addPlayer(t, ty);
				a.addPlayer(ct, cty);
			}
		} catch (Exception ex) {
			log(ex);
		}
	}

	
	private void parseTagAssociation(JSONObject result, String jsonObjectName, TopicMap tm, Topic ct, Topic cty) {
		try {
			String s = result.getString(jsonObjectName);
			if (s != null && s.length() > 0) {
				Topic t = getTagTopic(tm, jsonObjectName, s);
				Topic ty = getTagType(tm, jsonObjectName);
				Association a = tm.createAssociation(ty);
				a.addPlayer(ct, cty);
				a.addPlayer(t, ty);
			}
		} catch (Exception ex) {
			log(ex);
		}
	}
}
