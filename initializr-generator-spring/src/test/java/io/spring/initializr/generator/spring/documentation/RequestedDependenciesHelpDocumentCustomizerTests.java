/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.initializr.generator.spring.documentation;

import java.util.Arrays;

import io.spring.initializr.generator.io.template.MustacheTemplateRenderer;
import io.spring.initializr.generator.io.text.BulletedSection;
import io.spring.initializr.generator.project.ProjectDescription;
import io.spring.initializr.generator.project.ResolvedProjectDescription;
import io.spring.initializr.generator.test.InitializrMetadataTestBuilder;
import io.spring.initializr.metadata.Dependency;
import io.spring.initializr.metadata.InitializrMetadata;
import io.spring.initializr.metadata.Link;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link RequestedDependenciesHelpDocumentCustomizer}.
 *
 * @author Stephane Nicoll
 */
class RequestedDependenciesHelpDocumentCustomizerTests {

	private final InitializrMetadataTestBuilder metadataBuilder = InitializrMetadataTestBuilder.withDefaults();

	@Test
	void dependencyWithReferenceDocLink() {
		Dependency dependency = createDependency("example",
				Link.create("reference", "https://example.com/doc", "Reference doc example"));
		this.metadataBuilder.addDependencyGroup("test", dependency);
		HelpDocument document = customizeHelp("example");
		assertThat(document.gettingStarted().isEmpty()).isFalse();
		assertSingleLink(document.gettingStarted().referenceDocs(), "https://example.com/doc", "Reference doc example");
	}

	@Test
	void dependencyWithReferenceDocLinkGetDependencyNameByDefault() {
		Dependency dependency = createDependency("example", Link.create("reference", "https://example.com/doc"));
		dependency.setName("Example Library");
		this.metadataBuilder.addDependencyGroup("test", dependency);
		HelpDocument document = customizeHelp("example");
		assertThat(document.gettingStarted().isEmpty()).isFalse();
		assertSingleLink(document.gettingStarted().referenceDocs(), "https://example.com/doc", "Example Library");
	}

	@Test
	void dependencyWithSeveralReferenceDocLinksDoNotGetDependencyNameByDefault() {
		Dependency dependency = createDependency("example", Link.create("reference", "https://example.com/doc"),
				Link.create("reference", "https://example.com/doc2"));
		dependency.setName("Example Library");
		this.metadataBuilder.addDependencyGroup("test", dependency);
		HelpDocument document = customizeHelp("example");
		assertThat(document.gettingStarted().isEmpty()).isTrue();
	}

	@Test
	void dependencyWithGuideLink() {
		Dependency dependency = createDependency("example",
				Link.create("guide", "https://example.com/how-to", "How-to example"));
		this.metadataBuilder.addDependencyGroup("test", dependency);
		HelpDocument document = customizeHelp("example");
		assertThat(document.gettingStarted().isEmpty()).isFalse();
		assertSingleLink(document.gettingStarted().guides(), "https://example.com/how-to", "How-to example");
	}

	@Test
	void dependencyWithGuideLinkGetDependencyNameByDefault() {
		Dependency dependency = createDependency("example", Link.create("guide", "https://example.com/how-to"));
		dependency.setName("Example Library");
		this.metadataBuilder.addDependencyGroup("test", dependency);
		HelpDocument document = customizeHelp("example");
		assertThat(document.gettingStarted().isEmpty()).isFalse();
		assertSingleLink(document.gettingStarted().guides(), "https://example.com/how-to", "Example Library");
	}

	@Test
	void dependencyWithSeveralGuideLinksDoNotGetDependencyNameByDefault() {
		Dependency dependency = createDependency("example", Link.create("guide", "https://example.com/how-to"),
				Link.create("guide", "https://example.com/anothero"));
		dependency.setName("Example Library");
		this.metadataBuilder.addDependencyGroup("test", dependency);
		HelpDocument document = customizeHelp("example");
		assertThat(document.gettingStarted().isEmpty()).isTrue();
	}

	@Test
	void dependencyWithAdditionalLink() {
		Dependency dependency = createDependency("example",
				Link.create("something", "https://example.com/test", "Test App"));
		this.metadataBuilder.addDependencyGroup("test", dependency);
		HelpDocument document = customizeHelp("example");
		assertThat(document.gettingStarted().isEmpty()).isFalse();
		assertSingleLink(document.gettingStarted().additionalLinks(), "https://example.com/test", "Test App");
	}

	@Test
	void dependencyWithAdditionalLinkDoNotDependencyNameByDefault() {
		Dependency dependency = createDependency("example", Link.create("something", "https://example.com/test"));
		dependency.setName("Example Library");
		this.metadataBuilder.addDependencyGroup("test", dependency);
		HelpDocument document = customizeHelp("example");
		assertThat(document.gettingStarted().isEmpty()).isTrue();
	}

	private Dependency createDependency(String id, Link... links) {
		Dependency dependency = Dependency.withId(id, "com.example", "example");
		dependency.getLinks().addAll(Arrays.asList(links));
		return dependency;
	}

	private void assertSingleLink(BulletedSection<GettingStartedSection.Link> links, String href, String description) {
		assertThat(links.getItems()).hasSize(1);
		assertLink(links.getItems().get(0), href, description);
	}

	private void assertLink(GettingStartedSection.Link link, String href, String description) {
		assertThat(link.getHref()).isEqualTo(href);
		assertThat(link.getDescription()).isEqualTo(description);
	}

	private HelpDocument customizeHelp(String... requestedDependencies) {
		ProjectDescription description = new ProjectDescription();
		for (String requestedDependency : requestedDependencies) {
			description.addDependency(requestedDependency, null);
		}
		InitializrMetadata metadata = this.metadataBuilder.build();
		HelpDocument document = new HelpDocument(new MustacheTemplateRenderer("classpath:/templates"));
		new RequestedDependenciesHelpDocumentCustomizer(new ResolvedProjectDescription(description), metadata)
				.customize(document);
		return document;
	}

}
