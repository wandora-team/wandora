var mockConfig = {
	"dataUrl" : "ArtOfNoise.jtm",
	"sis": {
		"typeInstance": "http://psi.topicmaps.org/iso13250/model/type-instance",
		"type": "http://psi.topicmaps.org/iso13250/model/type",
		"instance": "http://psi.topicmaps.org/iso13250/model/instance",
		"superSub": "http://www.topicmaps.org/xtm/1.0/core.xtm#superclass-subclass",
		"superr": "http://www.topicmaps.org/xtm/1.0/core.xtm#superclass",
		"sub": "http://www.topicmaps.org/xtm/1.0/core.xtm#subclass",
		"root": "http://wandora.org/si/core/wandora-class"
	},
	"langs" : {
		"en": "http://www.topicmaps.org/xtm/1.0/language.xtm#en",
		"fi": "http://www.topicmaps.org/xtm/1.0/language.xtm#fi",
		"se": "http://www.topicmaps.org/xtm/1.0/language.xtm#sv",
		"independent" : "http://wandora.org/si/core/lang-independent"

	},
	"templates" : {
		"default" : {
			"controller" : "topicDetailControl",
			"view"		 : "topicDetail"
		},
		"types" : {

		},
		"topics" : {
			"http://wandora.org/si/core/wandora-class" : {
				"controller" : "topicDetailControl",
				"view"		 : "topicDetail"
			}
		}

	},
	"defaultLang" : "http://www.topicmaps.org/xtm/1.0/language.xtm#en",
	"translation" : [
		{
			"en" : "Classes",
			"fi" : "Tyypit"
		},
		{
			"en" : "Instances",
			"fi" : "Instanssit"
		},
		{
			"en" : "Subclasses",
			"fi" : "Aliluokat"
		},
		{
			"en" : "Superclasses",
			"fi" : "Yliluokat"
		}
	]
}


var mockJTM = {
	"version": "1.0",
	"item_type": "topicmap",
	"topics": [{
		"subject_identifiers": [
			"http://wandora.org/si/core/lang-independent"
		],
		"names": [{
			"value": "Language independent",
			"variants": [{
				"value": "Language independent",
				"scope": [
					"si:http://www.topicmaps.org/xtm/1.0/language.xtm#en",
					"si:http://www.topicmaps.org/xtm/1.0/core.xtm#display"
				]
			}]
		}]
	}, {
		"subject_identifiers": [
			"http://www.topicmaps.org/xtm/1.0/core.xtm#superclass-subclass"
		],
		"names": [{
			"value": "Superclass-Subclass"
		}],
		"occurrences": [{
			"value": "1",
			"type": "si:http://wandora.org/si/core/hidelevel",
			"scope": ["si:http://wandora.org/si/core/lang-independent"]
		}]
	}, {
		"subject_identifiers": [
			"http://wandora.org/si/core/schema-type"
		],
		"names": [{
			"value": "Schema type",
			"variants": [{
				"value": "Schema type",
				"scope": [
					"si:http://www.topicmaps.org/xtm/1.0/language.xtm#en",
					"si:http://www.topicmaps.org/xtm/1.0/core.xtm#display"
				]
			}]
		}],
		"occurrences": [{
			"value": "1",
			"type": "si:http://wandora.org/si/core/hidelevel",
			"scope": ["si:http://wandora.org/si/core/lang-independent"]
		}]
	}, {
		"subject_identifiers": [
			"http://wandora.org/si/core/default-role-1"
		],
		"names": [{
			"value": "Default role 1"
		}]
	}, {
		"subject_identifiers": [
			"http://processing.org"
		],
		"names": [{
			"value": "Processing type"
		}]
	}, {
		"subject_identifiers": [
			"http://www.r-project.org"
		],
		"names": [{
			"value": "R language type"
		}]
	}, {
		"subject_identifiers": [
			"http://www.topicmaps.org/xtm/1.0/core.xtm#subclass"
		],
		"names": [{
			"value": "Subclass"
		}],
		"occurrences": [{
			"value": "1",
			"type": "si:http://wandora.org/si/core/hidelevel",
			"scope": ["si:http://wandora.org/si/core/lang-independent"]
		}]
	}, {
		"subject_identifiers": [
			"http://wandora.org/si/core/variant-name-version"
		],
		"names": [{
			"value": "Wandora variant name version",
			"variants": [{
				"value": "Wandora variant name version",
				"scope": [
					"si:http://www.topicmaps.org/xtm/1.0/language.xtm#en",
					"si:http://www.topicmaps.org/xtm/1.0/core.xtm#display"
				]
			}]
		}]
	}, {
		"subject_identifiers": [
			"http://www.topicmaps.org/xtm/1.0/language.xtm#sv"
		],
		"names": [{
			"value": "Swedish language",
			"variants": [{
				"value": "Swedish",
				"scope": [
					"si:http://www.topicmaps.org/xtm/1.0/language.xtm#en",
					"si:http://www.topicmaps.org/xtm/1.0/core.xtm#display"
				]
			}]
		}]
	}, {
		"subject_identifiers": [
			"http://www.topicmaps.org/xtm/1.0/language.xtm#en"
		],
		"names": [{
			"value": "English language",
			"variants": [{
				"value": "English",
				"scope": [
					"si:http://www.topicmaps.org/xtm/1.0/language.xtm#en",
					"si:http://www.topicmaps.org/xtm/1.0/core.xtm#display"
				]
			}]
		}]
	}, {
		"subject_identifiers": [
			"http://wandora.org/si/core/default-role-2"
		],
		"names": [{
			"value": "Default role 2"
		}]
	}, {
		"subject_identifiers": [
			"http://wandora.org/si/core/hidelevel"
		],
		"names": [{
			"value": "Hide level",
			"variants": [{
				"value": "Hide level",
				"scope": [
					"si:http://www.topicmaps.org/xtm/1.0/language.xtm#en",
					"si:http://www.topicmaps.org/xtm/1.0/core.xtm#display"
				]
			}]
		}],
		"occurrences": [{
			"value": "1",
			"type": "si:http://wandora.org/si/core/hidelevel",
			"scope": ["si:http://wandora.org/si/core/lang-independent"]
		}]
	}, {
		"subject_identifiers": [
			"http://wandora.org/si/core/language",
			"http://www.topicmaps.org/xtm/1.0/language.xtm"
		],
		"names": [{
			"value": "Wandora language",
			"variants": [{
				"value": "Wandora language",
				"scope": [
					"si:http://www.topicmaps.org/xtm/1.0/language.xtm#en",
					"si:http://www.topicmaps.org/xtm/1.0/core.xtm#display"
				]
			}]
		}]
	}, {
		"subject_identifiers": [
			"http://www.topicmaps.org/xtm/1.0/core.xtm#sort"
		],
		"names": [{
			"value": "Scope Sort",
			"variants": [{
				"value": "Sort name",
				"scope": [
					"si:http://www.topicmaps.org/xtm/1.0/language.xtm#en",
					"si:http://www.topicmaps.org/xtm/1.0/core.xtm#display"
				]
			}]
		}]
	}, {
		"subject_identifiers": [
			"http://wandora.org/si/core/occurrence-type"
		],
		"names": [{
			"value": "Occurrence type",
			"variants": [{
				"value": "Occurrence type",
				"scope": [
					"si:http://www.topicmaps.org/xtm/1.0/language.xtm#en",
					"si:http://www.topicmaps.org/xtm/1.0/core.xtm#display"
				]
			}]
		}],
		"occurrences": [{
			"value": "1",
			"type": "si:http://wandora.org/si/core/hidelevel",
			"scope": ["si:http://wandora.org/si/core/lang-independent"]
		}]
	}, {
		"subject_identifiers": [
			"http://wandora.org/si/core/edittime"
		],
		"names": [{
			"value": "Topic edit time"
		}],
		"occurrences": [{
			"value": "1",
			"type": "si:http://wandora.org/si/core/hidelevel",
			"scope": ["si:http://wandora.org/si/core/lang-independent"]
		}]
	}, {
		"subject_identifiers": [
			"http://wandora.org/si/core/association-type"
		],
		"names": [{
			"value": "Association type",
			"variants": [{
				"value": "Association type",
				"scope": [
					"si:http://www.topicmaps.org/xtm/1.0/language.xtm#en",
					"si:http://www.topicmaps.org/xtm/1.0/core.xtm#display"
				]
			}]
		}],
		"occurrences": [{
			"value": "1",
			"type": "si:http://wandora.org/si/core/hidelevel",
			"scope": ["si:http://wandora.org/si/core/lang-independent"]
		}]
	}, {
		"subject_identifiers": [
			"http://wandora.org/si/core/default-occurrence"
		],
		"names": [{
			"value": "Default occurrence"
		}]
	}, {
		"subject_identifiers": [
			"http://wandora.org/si/core/default-association"
		],
		"names": [{
			"value": "Default association"
		}]
	}, {
		"subject_identifiers": [
			"http://www.topicmaps.org/xtm/1.0/core.xtm#display"
		],
		"names": [{
			"value": "Scope Display",
			"variants": [{
				"value": "Display name",
				"scope": [
					"si:http://www.topicmaps.org/xtm/1.0/language.xtm#en",
					"si:http://www.topicmaps.org/xtm/1.0/core.xtm#display"
				]
			}]
		}]
	}, {
		"subject_identifiers": [
			"http://www.topicmaps.org/xtm/1.0/language.xtm#fi"
		],
		"names": [{
			"value": "Finnish language",
			"variants": [{
				"value": "Finnish",
				"scope": [
					"si:http://www.topicmaps.org/xtm/1.0/language.xtm#en",
					"si:http://www.topicmaps.org/xtm/1.0/core.xtm#display"
				]
			}]
		}]
	}, {
		"subject_identifiers": [
			"http://wandora.org/si/core/content-type"
		],
		"names": [{
			"value": "Content type",
			"variants": [{
				"value": "Content type",
				"scope": [
					"si:http://www.topicmaps.org/xtm/1.0/language.xtm#en",
					"si:http://www.topicmaps.org/xtm/1.0/core.xtm#display"
				]
			}]
		}],
		"occurrences": [{
			"value": "1",
			"type": "si:http://wandora.org/si/core/hidelevel",
			"scope": ["si:http://wandora.org/si/core/lang-independent"]
		}]
	}, {
		"subject_identifiers": [
			"http://wandora.org/si/core/role-class"
		],
		"names": [{
			"value": "Role class",
			"variants": [{
				"value": "Role class",
				"scope": [
					"si:http://www.topicmaps.org/xtm/1.0/language.xtm#en",
					"si:http://www.topicmaps.org/xtm/1.0/core.xtm#display"
				]
			}]
		}],
		"occurrences": [{
			"value": "1",
			"type": "si:http://wandora.org/si/core/hidelevel",
			"scope": ["si:http://wandora.org/si/core/lang-independent"]
		}]
	}, {
		"subject_identifiers": [
			"http://www.topicmaps.org/xtm/1.0/core.xtm#superclass"
		],
		"names": [{
			"value": "Superclass"
		}],
		"occurrences": [{
			"value": "1",
			"type": "si:http://wandora.org/si/core/hidelevel",
			"scope": ["si:http://wandora.org/si/core/lang-independent"]
		}]
	}, {
		"subject_identifiers": [
			"http://wandora.org/si/core/role"
		],
		"names": [{
			"value": "Role",
			"variants": [{
				"value": "Role",
				"scope": [
					"si:http://www.topicmaps.org/xtm/1.0/language.xtm#en",
					"si:http://www.topicmaps.org/xtm/1.0/core.xtm#display"
				]
			}]
		}],
		"occurrences": [{
			"value": "1",
			"type": "si:http://wandora.org/si/core/hidelevel",
			"scope": ["si:http://wandora.org/si/core/lang-independent"]
		}]
	}, {
		"subject_identifiers": [
			"http://wandora.org/si/core/wandora-class"
		],
		"names": [{
			"value": "Wandora class",
			"variants": [{
				"value": "Wandora class",
				"scope": [
					"si:http://www.topicmaps.org/xtm/1.0/language.xtm#en",
					"si:http://www.topicmaps.org/xtm/1.0/core.xtm#display"
				]
			}]
		}],
		"occurrences": [{
			"value": "1",
			"type": "si:http://wandora.org/si/core/hidelevel",
			"scope": ["si:http://wandora.org/si/core/lang-independent"]
		}]
	}],
	"associations": [{
		"type": "si:http://psi.topicmaps.org/iso13250/model/type-instance",
		"roles": [{
			"player": "si:http://wandora.org/si/core/language",
			"type": "si:http://psi.topicmaps.org/iso13250/model/type"
		}, {
			"player": "si:http://wandora.org/si/core/lang-independent",
			"type": "si:http://psi.topicmaps.org/iso13250/model/instance"
		}]
	}, {
		"type": "si:http://psi.topicmaps.org/iso13250/model/type-instance",
		"roles": [{
			"player": "si:http://wandora.org/si/core/association-type",
			"type": "si:http://psi.topicmaps.org/iso13250/model/type"
		}, {
			"player": "si:http://www.topicmaps.org/xtm/1.0/core.xtm#superclass-subclass",
			"type": "si:http://psi.topicmaps.org/iso13250/model/instance"
		}]
	}, {
		"type": "si:http://psi.topicmaps.org/iso13250/model/type-instance",
		"roles": [{
			"player": "si:http://wandora.org/si/core/content-type",
			"type": "si:http://psi.topicmaps.org/iso13250/model/type"
		}, {
			"player": "si:http://wandora.org/si/core/schema-type",
			"type": "si:http://psi.topicmaps.org/iso13250/model/instance"
		}]
	}, {
		"type": "si:http://psi.topicmaps.org/iso13250/model/type-instance",
		"roles": [{
			"player": "si:http://wandora.org/si/core/role-class",
			"type": "si:http://psi.topicmaps.org/iso13250/model/type"
		}, {
			"player": "si:http://wandora.org/si/core/schema-type",
			"type": "si:http://psi.topicmaps.org/iso13250/model/instance"
		}]
	}, {
		"type": "si:http://psi.topicmaps.org/iso13250/model/type-instance",
		"roles": [{
			"player": "si:http://wandora.org/si/core/schema-type",
			"type": "si:http://psi.topicmaps.org/iso13250/model/type"
		}, {
			"player": "si:http://wandora.org/si/core/schema-type",
			"type": "si:http://psi.topicmaps.org/iso13250/model/instance"
		}]
	}, {
		"type": "si:http://psi.topicmaps.org/iso13250/model/type-instance",
		"roles": [{
			"player": "si:http://wandora.org/si/core/role",
			"type": "si:http://psi.topicmaps.org/iso13250/model/type"
		}, {
			"player": "si:http://wandora.org/si/core/default-role-1",
			"type": "si:http://psi.topicmaps.org/iso13250/model/instance"
		}]
	}, {
		"type": "si:http://psi.topicmaps.org/iso13250/model/type-instance",
		"roles": [{
			"player": "si:http://wandora.org/si/core/occurrence-type",
			"type": "si:http://psi.topicmaps.org/iso13250/model/type"
		}, {
			"player": "si:http://processing.org",
			"type": "si:http://psi.topicmaps.org/iso13250/model/instance"
		}]
	}, {
		"type": "si:http://psi.topicmaps.org/iso13250/model/type-instance",
		"roles": [{
			"player": "si:http://wandora.org/si/core/occurrence-type",
			"type": "si:http://psi.topicmaps.org/iso13250/model/type"
		}, {
			"player": "si:http://www.r-project.org",
			"type": "si:http://psi.topicmaps.org/iso13250/model/instance"
		}]
	}, {
		"type": "si:http://psi.topicmaps.org/iso13250/model/type-instance",
		"roles": [{
			"player": "si:http://wandora.org/si/core/role",
			"type": "si:http://psi.topicmaps.org/iso13250/model/type"
		}, {
			"player": "si:http://www.topicmaps.org/xtm/1.0/core.xtm#subclass",
			"type": "si:http://psi.topicmaps.org/iso13250/model/instance"
		}]
	}, {
		"type": "si:http://psi.topicmaps.org/iso13250/model/type-instance",
		"roles": [{
			"player": "si:http://wandora.org/si/core/schema-type",
			"type": "si:http://psi.topicmaps.org/iso13250/model/type"
		}, {
			"player": "si:http://wandora.org/si/core/variant-name-version",
			"type": "si:http://psi.topicmaps.org/iso13250/model/instance"
		}]
	}, {
		"type": "si:http://psi.topicmaps.org/iso13250/model/type-instance",
		"roles": [{
			"player": "si:http://wandora.org/si/core/language",
			"type": "si:http://psi.topicmaps.org/iso13250/model/type"
		}, {
			"player": "si:http://www.topicmaps.org/xtm/1.0/language.xtm#sv",
			"type": "si:http://psi.topicmaps.org/iso13250/model/instance"
		}]
	}, {
		"type": "si:http://psi.topicmaps.org/iso13250/model/type-instance",
		"roles": [{
			"player": "si:http://wandora.org/si/core/language",
			"type": "si:http://psi.topicmaps.org/iso13250/model/type"
		}, {
			"player": "si:http://www.topicmaps.org/xtm/1.0/language.xtm#en",
			"type": "si:http://psi.topicmaps.org/iso13250/model/instance"
		}]
	}, {
		"type": "si:http://psi.topicmaps.org/iso13250/model/type-instance",
		"roles": [{
			"player": "si:http://wandora.org/si/core/role",
			"type": "si:http://psi.topicmaps.org/iso13250/model/type"
		}, {
			"player": "si:http://wandora.org/si/core/default-role-2",
			"type": "si:http://psi.topicmaps.org/iso13250/model/instance"
		}]
	}, {
		"type": "si:http://psi.topicmaps.org/iso13250/model/type-instance",
		"roles": [{
			"player": "si:http://wandora.org/si/core/occurrence-type",
			"type": "si:http://psi.topicmaps.org/iso13250/model/type"
		}, {
			"player": "si:http://wandora.org/si/core/hidelevel",
			"type": "si:http://psi.topicmaps.org/iso13250/model/instance"
		}]
	}, {
		"type": "si:http://psi.topicmaps.org/iso13250/model/type-instance",
		"roles": [{
			"player": "si:http://wandora.org/si/core/schema-type",
			"type": "si:http://psi.topicmaps.org/iso13250/model/type"
		}, {
			"player": "si:http://wandora.org/si/core/language",
			"type": "si:http://psi.topicmaps.org/iso13250/model/instance"
		}]
	}, {
		"type": "si:http://psi.topicmaps.org/iso13250/model/type-instance",
		"roles": [{
			"player": "si:http://wandora.org/si/core/variant-name-version",
			"type": "si:http://psi.topicmaps.org/iso13250/model/type"
		}, {
			"player": "si:http://www.topicmaps.org/xtm/1.0/core.xtm#sort",
			"type": "si:http://psi.topicmaps.org/iso13250/model/instance"
		}]
	}, {
		"type": "si:http://psi.topicmaps.org/iso13250/model/type-instance",
		"roles": [{
			"player": "si:http://wandora.org/si/core/content-type",
			"type": "si:http://psi.topicmaps.org/iso13250/model/type"
		}, {
			"player": "si:http://wandora.org/si/core/occurrence-type",
			"type": "si:http://psi.topicmaps.org/iso13250/model/instance"
		}]
	}, {
		"type": "si:http://psi.topicmaps.org/iso13250/model/type-instance",
		"roles": [{
			"player": "si:http://wandora.org/si/core/role",
			"type": "si:http://psi.topicmaps.org/iso13250/model/type"
		}, {
			"player": "si:http://wandora.org/si/core/occurrence-type",
			"type": "si:http://psi.topicmaps.org/iso13250/model/instance"
		}]
	}, {
		"type": "si:http://psi.topicmaps.org/iso13250/model/type-instance",
		"roles": [{
			"player": "si:http://wandora.org/si/core/association-type",
			"type": "si:http://psi.topicmaps.org/iso13250/model/type"
		}, {
			"player": "si:http://wandora.org/si/core/occurrence-type",
			"type": "si:http://psi.topicmaps.org/iso13250/model/instance"
		}]
	}, {
		"type": "si:http://psi.topicmaps.org/iso13250/model/type-instance",
		"roles": [{
			"player": "si:http://wandora.org/si/core/schema-type",
			"type": "si:http://psi.topicmaps.org/iso13250/model/type"
		}, {
			"player": "si:http://wandora.org/si/core/occurrence-type",
			"type": "si:http://psi.topicmaps.org/iso13250/model/instance"
		}]
	}, {
		"type": "si:http://psi.topicmaps.org/iso13250/model/type-instance",
		"roles": [{
			"player": "si:http://wandora.org/si/core/content-type",
			"type": "si:http://psi.topicmaps.org/iso13250/model/type"
		}, {
			"player": "si:http://wandora.org/si/core/association-type",
			"type": "si:http://psi.topicmaps.org/iso13250/model/instance"
		}]
	}, {
		"type": "si:http://psi.topicmaps.org/iso13250/model/type-instance",
		"roles": [{
			"player": "si:http://wandora.org/si/core/role",
			"type": "si:http://psi.topicmaps.org/iso13250/model/type"
		}, {
			"player": "si:http://wandora.org/si/core/association-type",
			"type": "si:http://psi.topicmaps.org/iso13250/model/instance"
		}]
	}, {
		"type": "si:http://psi.topicmaps.org/iso13250/model/type-instance",
		"roles": [{
			"player": "si:http://wandora.org/si/core/association-type",
			"type": "si:http://psi.topicmaps.org/iso13250/model/type"
		}, {
			"player": "si:http://wandora.org/si/core/association-type",
			"type": "si:http://psi.topicmaps.org/iso13250/model/instance"
		}]
	}, {
		"type": "si:http://psi.topicmaps.org/iso13250/model/type-instance",
		"roles": [{
			"player": "si:http://wandora.org/si/core/schema-type",
			"type": "si:http://psi.topicmaps.org/iso13250/model/type"
		}, {
			"player": "si:http://wandora.org/si/core/association-type",
			"type": "si:http://psi.topicmaps.org/iso13250/model/instance"
		}]
	}, {
		"type": "si:http://psi.topicmaps.org/iso13250/model/type-instance",
		"roles": [{
			"player": "si:http://wandora.org/si/core/occurrence-type",
			"type": "si:http://psi.topicmaps.org/iso13250/model/type"
		}, {
			"player": "si:http://wandora.org/si/core/default-occurrence",
			"type": "si:http://psi.topicmaps.org/iso13250/model/instance"
		}]
	}, {
		"type": "si:http://psi.topicmaps.org/iso13250/model/type-instance",
		"roles": [{
			"player": "si:http://wandora.org/si/core/association-type",
			"type": "si:http://psi.topicmaps.org/iso13250/model/type"
		}, {
			"player": "si:http://wandora.org/si/core/default-association",
			"type": "si:http://psi.topicmaps.org/iso13250/model/instance"
		}]
	}, {
		"type": "si:http://psi.topicmaps.org/iso13250/model/type-instance",
		"roles": [{
			"player": "si:http://wandora.org/si/core/variant-name-version",
			"type": "si:http://psi.topicmaps.org/iso13250/model/type"
		}, {
			"player": "si:http://www.topicmaps.org/xtm/1.0/core.xtm#display",
			"type": "si:http://psi.topicmaps.org/iso13250/model/instance"
		}]
	}, {
		"type": "si:http://psi.topicmaps.org/iso13250/model/type-instance",
		"roles": [{
			"player": "si:http://wandora.org/si/core/language",
			"type": "si:http://psi.topicmaps.org/iso13250/model/type"
		}, {
			"player": "si:http://www.topicmaps.org/xtm/1.0/language.xtm#fi",
			"type": "si:http://psi.topicmaps.org/iso13250/model/instance"
		}]
	}, {
		"type": "si:http://psi.topicmaps.org/iso13250/model/type-instance",
		"roles": [{
			"player": "si:http://wandora.org/si/core/content-type",
			"type": "si:http://psi.topicmaps.org/iso13250/model/type"
		}, {
			"player": "si:http://wandora.org/si/core/content-type",
			"type": "si:http://psi.topicmaps.org/iso13250/model/instance"
		}]
	}, {
		"type": "si:http://psi.topicmaps.org/iso13250/model/type-instance",
		"roles": [{
			"player": "si:http://wandora.org/si/core/role",
			"type": "si:http://psi.topicmaps.org/iso13250/model/type"
		}, {
			"player": "si:http://wandora.org/si/core/content-type",
			"type": "si:http://psi.topicmaps.org/iso13250/model/instance"
		}]
	}, {
		"type": "si:http://psi.topicmaps.org/iso13250/model/type-instance",
		"roles": [{
			"player": "si:http://wandora.org/si/core/schema-type",
			"type": "si:http://psi.topicmaps.org/iso13250/model/type"
		}, {
			"player": "si:http://wandora.org/si/core/content-type",
			"type": "si:http://psi.topicmaps.org/iso13250/model/instance"
		}]
	}, {
		"type": "si:http://psi.topicmaps.org/iso13250/model/type-instance",
		"roles": [{
			"player": "si:http://wandora.org/si/core/role",
			"type": "si:http://psi.topicmaps.org/iso13250/model/type"
		}, {
			"player": "si:http://wandora.org/si/core/role-class",
			"type": "si:http://psi.topicmaps.org/iso13250/model/instance"
		}]
	}, {
		"type": "si:http://psi.topicmaps.org/iso13250/model/type-instance",
		"roles": [{
			"player": "si:http://wandora.org/si/core/association-type",
			"type": "si:http://psi.topicmaps.org/iso13250/model/type"
		}, {
			"player": "si:http://wandora.org/si/core/role-class",
			"type": "si:http://psi.topicmaps.org/iso13250/model/instance"
		}]
	}, {
		"type": "si:http://psi.topicmaps.org/iso13250/model/type-instance",
		"roles": [{
			"player": "si:http://wandora.org/si/core/schema-type",
			"type": "si:http://psi.topicmaps.org/iso13250/model/type"
		}, {
			"player": "si:http://wandora.org/si/core/role-class",
			"type": "si:http://psi.topicmaps.org/iso13250/model/instance"
		}]
	}, {
		"type": "si:http://psi.topicmaps.org/iso13250/model/type-instance",
		"roles": [{
			"player": "si:http://wandora.org/si/core/role",
			"type": "si:http://psi.topicmaps.org/iso13250/model/type"
		}, {
			"player": "si:http://www.topicmaps.org/xtm/1.0/core.xtm#superclass",
			"type": "si:http://psi.topicmaps.org/iso13250/model/instance"
		}]
	}, {
		"type": "si:http://psi.topicmaps.org/iso13250/model/type-instance",
		"roles": [{
			"player": "si:http://wandora.org/si/core/content-type",
			"type": "si:http://psi.topicmaps.org/iso13250/model/type"
		}, {
			"player": "si:http://wandora.org/si/core/role",
			"type": "si:http://psi.topicmaps.org/iso13250/model/instance"
		}]
	}, {
		"type": "si:http://psi.topicmaps.org/iso13250/model/type-instance",
		"roles": [{
			"player": "si:http://wandora.org/si/core/role",
			"type": "si:http://psi.topicmaps.org/iso13250/model/type"
		}, {
			"player": "si:http://wandora.org/si/core/role",
			"type": "si:http://psi.topicmaps.org/iso13250/model/instance"
		}]
	}, {
		"type": "si:http://psi.topicmaps.org/iso13250/model/type-instance",
		"roles": [{
			"player": "si:http://wandora.org/si/core/association-type",
			"type": "si:http://psi.topicmaps.org/iso13250/model/type"
		}, {
			"player": "si:http://wandora.org/si/core/role",
			"type": "si:http://psi.topicmaps.org/iso13250/model/instance"
		}]
	}, {
		"type": "si:http://psi.topicmaps.org/iso13250/model/type-instance",
		"roles": [{
			"player": "si:http://wandora.org/si/core/schema-type",
			"type": "si:http://psi.topicmaps.org/iso13250/model/type"
		}, {
			"player": "si:http://wandora.org/si/core/role",
			"type": "si:http://psi.topicmaps.org/iso13250/model/instance"
		}]
	}, {
		"type": "si:http://psi.topicmaps.org/iso13250/model/type-instance",
		"roles": [{
			"player": "si:http://wandora.org/si/core/schema-type",
			"type": "si:http://psi.topicmaps.org/iso13250/model/type"
		}, {
			"player": "si:http://wandora.org/si/core/wandora-class",
			"type": "si:http://psi.topicmaps.org/iso13250/model/instance"
		}]
	}, {
		"type": "si:http://wandora.org/si/core/association-type",
		"roles": [{
			"player": "si:http://wandora.org/si/core/schema-type",
			"type": "si:http://wandora.org/si/core/content-type"
		}, {
			"player": "si:http://www.topicmaps.org/xtm/1.0/core.xtm#superclass-subclass",
			"type": "si:http://wandora.org/si/core/association-type"
		}]
	}, {
		"type": "si:http://wandora.org/si/core/role",
		"roles": [{
			"player": "si:http://www.topicmaps.org/xtm/1.0/core.xtm#subclass",
			"type": "si:http://wandora.org/si/core/role"
		}, {
			"player": "si:http://www.topicmaps.org/xtm/1.0/core.xtm#superclass-subclass",
			"type": "si:http://wandora.org/si/core/association-type"
		}]
	}, {
		"type": "si:http://wandora.org/si/core/role",
		"roles": [{
			"player": "si:http://www.topicmaps.org/xtm/1.0/core.xtm#superclass",
			"type": "si:http://wandora.org/si/core/role"
		}, {
			"player": "si:http://www.topicmaps.org/xtm/1.0/core.xtm#superclass-subclass",
			"type": "si:http://wandora.org/si/core/association-type"
		}]
	}, {
		"type": "si:http://wandora.org/si/core/role-class",
		"roles": [{
			"player": "si:http://www.topicmaps.org/xtm/1.0/core.xtm#subclass",
			"type": "si:http://wandora.org/si/core/role"
		}, {
			"player": "si:http://wandora.org/si/core/schema-type",
			"type": "si:http://wandora.org/si/core/role-class"
		}]
	}, {
		"type": "si:http://wandora.org/si/core/occurrence-type",
		"roles": [{
			"player": "si:http://wandora.org/si/core/schema-type",
			"type": "si:http://wandora.org/si/core/content-type"
		}, {
			"player": "si:http://wandora.org/si/core/hidelevel",
			"type": "si:http://wandora.org/si/core/occurrence-type"
		}]
	}, {
		"type": "si:http://www.topicmaps.org/xtm/1.0/core.xtm#superclass-subclass",
		"roles": [{
			"player": "si:http://wandora.org/si/core/schema-type",
			"type": "si:http://www.topicmaps.org/xtm/1.0/core.xtm#superclass"
		}, {
			"player": "si:http://wandora.org/si/core/role-class",
			"type": "si:http://www.topicmaps.org/xtm/1.0/core.xtm#subclass"
		}]
	}, {
		"type": "si:http://www.topicmaps.org/xtm/1.0/core.xtm#superclass-subclass",
		"roles": [{
			"player": "si:http://wandora.org/si/core/schema-type",
			"type": "si:http://www.topicmaps.org/xtm/1.0/core.xtm#superclass"
		}, {
			"player": "si:http://wandora.org/si/core/role",
			"type": "si:http://www.topicmaps.org/xtm/1.0/core.xtm#subclass"
		}]
	}, {
		"type": "si:http://wandora.org/si/core/role-class",
		"roles": [{
			"player": "si:http://www.topicmaps.org/xtm/1.0/core.xtm#superclass",
			"type": "si:http://wandora.org/si/core/role"
		}, {
			"player": "si:http://wandora.org/si/core/schema-type",
			"type": "si:http://wandora.org/si/core/role-class"
		}]
	}, {
		"type": "si:http://www.topicmaps.org/xtm/1.0/core.xtm#superclass-subclass",
		"roles": [{
			"player": "si:http://wandora.org/si/core/wandora-class",
			"type": "si:http://www.topicmaps.org/xtm/1.0/core.xtm#superclass"
		}, {
			"player": "si:http://wandora.org/si/core/schema-type",
			"type": "si:http://www.topicmaps.org/xtm/1.0/core.xtm#subclass"
		}]
	}, {
		"type": "si:http://www.topicmaps.org/xtm/1.0/core.xtm#superclass-subclass",
		"roles": [{
			"player": "si:http://wandora.org/si/core/schema-type",
			"type": "si:http://www.topicmaps.org/xtm/1.0/core.xtm#superclass"
		}, {
			"player": "si:http://wandora.org/si/core/occurrence-type",
			"type": "si:http://www.topicmaps.org/xtm/1.0/core.xtm#subclass"
		}]
	}, {
		"type": "si:http://www.topicmaps.org/xtm/1.0/core.xtm#superclass-subclass",
		"roles": [{
			"player": "si:http://wandora.org/si/core/schema-type",
			"type": "si:http://www.topicmaps.org/xtm/1.0/core.xtm#superclass"
		}, {
			"player": "si:http://wandora.org/si/core/association-type",
			"type": "si:http://www.topicmaps.org/xtm/1.0/core.xtm#subclass"
		}]
	}, {
		"type": "si:http://www.topicmaps.org/xtm/1.0/core.xtm#superclass-subclass",
		"roles": [{
			"player": "si:http://wandora.org/si/core/schema-type",
			"type": "si:http://www.topicmaps.org/xtm/1.0/core.xtm#superclass"
		}, {
			"player": "si:http://wandora.org/si/core/content-type",
			"type": "si:http://www.topicmaps.org/xtm/1.0/core.xtm#subclass"
		}]
	}, {
		"type": "si:http://wandora.org/si/core/association-type",
		"roles": [{
			"player": "si:http://wandora.org/si/core/content-type",
			"type": "si:http://wandora.org/si/core/content-type"
		}, {
			"player": "si:http://wandora.org/si/core/occurrence-type",
			"type": "si:http://wandora.org/si/core/association-type"
		}]
	}, {
		"type": "si:http://wandora.org/si/core/role",
		"roles": [{
			"player": "si:http://wandora.org/si/core/content-type",
			"type": "si:http://wandora.org/si/core/role"
		}, {
			"player": "si:http://wandora.org/si/core/occurrence-type",
			"type": "si:http://wandora.org/si/core/association-type"
		}]
	}, {
		"type": "si:http://wandora.org/si/core/role",
		"roles": [{
			"player": "si:http://wandora.org/si/core/occurrence-type",
			"type": "si:http://wandora.org/si/core/role"
		}, {
			"player": "si:http://wandora.org/si/core/occurrence-type",
			"type": "si:http://wandora.org/si/core/association-type"
		}]
	}, {
		"type": "si:http://wandora.org/si/core/role",
		"roles": [{
			"player": "si:http://wandora.org/si/core/content-type",
			"type": "si:http://wandora.org/si/core/role"
		}, {
			"player": "si:http://wandora.org/si/core/association-type",
			"type": "si:http://wandora.org/si/core/association-type"
		}]
	}, {
		"type": "si:http://wandora.org/si/core/role",
		"roles": [{
			"player": "si:http://wandora.org/si/core/association-type",
			"type": "si:http://wandora.org/si/core/role"
		}, {
			"player": "si:http://wandora.org/si/core/association-type",
			"type": "si:http://wandora.org/si/core/association-type"
		}]
	}, {
		"type": "si:http://wandora.org/si/core/association-type",
		"roles": [{
			"player": "si:http://wandora.org/si/core/content-type",
			"type": "si:http://wandora.org/si/core/content-type"
		}, {
			"player": "si:http://wandora.org/si/core/association-type",
			"type": "si:http://wandora.org/si/core/association-type"
		}]
	}, {
		"type": "si:http://wandora.org/si/core/role",
		"roles": [{
			"player": "si:http://wandora.org/si/core/association-type",
			"type": "si:http://wandora.org/si/core/role"
		}, {
			"player": "si:http://wandora.org/si/core/role",
			"type": "si:http://wandora.org/si/core/association-type"
		}]
	}, {
		"type": "si:http://wandora.org/si/core/association-type",
		"roles": [{
			"player": "si:http://wandora.org/si/core/association-type",
			"type": "si:http://wandora.org/si/core/content-type"
		}, {
			"player": "si:http://wandora.org/si/core/role",
			"type": "si:http://wandora.org/si/core/association-type"
		}]
	}, {
		"type": "si:http://wandora.org/si/core/role",
		"roles": [{
			"player": "si:http://wandora.org/si/core/role-class",
			"type": "si:http://wandora.org/si/core/role"
		}, {
			"player": "si:http://wandora.org/si/core/role-class",
			"type": "si:http://wandora.org/si/core/association-type"
		}]
	}, {
		"type": "si:http://wandora.org/si/core/role",
		"roles": [{
			"player": "si:http://wandora.org/si/core/role",
			"type": "si:http://wandora.org/si/core/role"
		}, {
			"player": "si:http://wandora.org/si/core/role-class",
			"type": "si:http://wandora.org/si/core/association-type"
		}]
	}, {
		"type": "si:http://wandora.org/si/core/association-type",
		"roles": [{
			"player": "si:http://wandora.org/si/core/role",
			"type": "si:http://wandora.org/si/core/content-type"
		}, {
			"player": "si:http://wandora.org/si/core/role-class",
			"type": "si:http://wandora.org/si/core/association-type"
		}]
	}, {
		"type": "si:http://wandora.org/si/core/role",
		"roles": [{
			"player": "si:http://wandora.org/si/core/role",
			"type": "si:http://wandora.org/si/core/role"
		}, {
			"player": "si:http://wandora.org/si/core/role",
			"type": "si:http://wandora.org/si/core/association-type"
		}]
	}]
};