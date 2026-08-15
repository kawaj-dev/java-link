(() => {
    "use strict";

    const optionArea = document.querySelector("[data-interactive-answer-url]");
    if (!optionArea) {
        return;
    }

    const forms = Array.from(
        optionArea.querySelectorAll("form[data-quiz-answer-form='true']")
    );
    const reduceMotion = window.matchMedia(
        "(prefers-reduced-motion: reduce)"
    ).matches;
    let answering = false;

    forms.forEach((form) => {
        form.addEventListener("submit", async (event) => {
            event.preventDefault();
            if (answering) {
                return;
            }

            const button = event.submitter || form.querySelector("button");
            if (!button) {
                return;
            }

            answering = true;
            optionArea.setAttribute("aria-busy", "true");
            setAnswerCardsDisabled(true);

            try {
                // submitボタンのname/valueはFormData(form)だけでは含まれないため、
                // 実際に押されたカードの識別情報を明示して送ります。
                const body = new URLSearchParams();
                body.set(button.name, button.value);
                const targetStep = form.querySelector("[name='targetStepId']");
                body.set("targetStepId", targetStep ? targetStep.value : "");
                const response = await fetch(
                    optionArea.dataset.interactiveAnswerUrl,
                    {
                        method: "POST",
                        headers: {
                            "Content-Type": "application/x-www-form-urlencoded",
                            "Accept": "application/json"
                        },
                        body
                    }
                );

                if (!response.ok) {
                    throw new Error("回答結果を取得できませんでした。");
                }

                const result = await response.json();
                if (!result.correct) {
                    showIncorrect(button);
                    return;
                }

                markAnsweredButton(button, result.answeredStepId);
                await lightBulb(result.answeredStepId);
                await revealCorrectMeaning(result);
                showExplanation(result);
                updateProgress(result.completedCount);
                if (result.partCompleted) {
                    showPartComplete();
                } else {
                    enableNextStep(result.nextStepId);
                }
                answering = false;
                optionArea.setAttribute("aria-busy", "false");
            } catch (error) {
                answering = false;
                optionArea.setAttribute("aria-busy", "false");
                setAnswerCardsDisabled(false);
            }
        });
    });

    function setAnswerCardsDisabled(disabled) {
        forms.forEach((form) => {
            const button = form.querySelector("button");
            if (button) {
                const canAnswer = button.dataset.answerEnabled === "true";
                button.disabled = disabled || !canAnswer;
                button.setAttribute("aria-disabled", String(button.disabled));
            }
        });
    }

    function markAnsweredButton(button, stepId) {
        document.querySelectorAll(".quiz-circuit-code-button--explaining")
            .forEach((item) => {
                item.classList.remove("quiz-circuit-code-button--explaining");
                item.removeAttribute("aria-current");
                const code = item.querySelector(".quiz-circuit-code-text")
                    ?.textContent?.trim();
                if (code) {
                    item.setAttribute("aria-label", `${code}は確認済み`);
                }
            });
        document.querySelectorAll(".quiz-part-circuit-step--explaining")
            .forEach((item) => item.classList.remove("quiz-part-circuit-step--explaining"));
        document.querySelectorAll(".quiz-reading-code-unit--current")
            .forEach((item) => item.classList.remove("quiz-reading-code-unit--current"));
        button.classList.remove(
            "quiz-circuit-code-button--next",
            "quiz-circuit-code-button--locked"
        );
        button.classList.add(
            "quiz-circuit-code-button--completed",
            "quiz-circuit-code-button--explaining"
        );
        button.dataset.answerEnabled = "false";
        button.disabled = true;
        button.setAttribute("aria-disabled", "true");
        button.setAttribute("aria-pressed", "true");
        button.setAttribute("aria-current", "step");
        const circuitStep = button.closest(".quiz-part-circuit-step");
        circuitStep?.classList.remove(
            "quiz-part-circuit-step--next",
            "quiz-part-circuit-step--locked"
        );
        circuitStep?.classList.add(
            "quiz-part-circuit-step--completed",
            "quiz-part-circuit-step--explaining"
        );
        const stateLabel = circuitStep?.querySelector(".visually-hidden");
        if (stateLabel) {
            const codeText = button.querySelector(".quiz-circuit-code-text")?.textContent.trim() || "";
            stateLabel.textContent = `${codeText} 完了`;
            button.setAttribute("aria-label", `${codeText}の説明を表示中`);
        }
        const codeUnit = document.querySelector(
            `[data-step-id="${CSS.escape(stepId)}"]`
        );
        if (codeUnit) {
            codeUnit.classList.add(
                "quiz-reading-code-unit--completed",
                "quiz-reading-code-unit--current"
            );
        }
    }

    function showExplanation(result) {
        const panel = document.querySelector("[data-answer-explanation]");
        if (!panel) {
            return;
        }
        const sections = panel.querySelector("[data-explanation-sections]");
        if (sections) {
            const explanationSections = Array.isArray(result.explanationSections)
                ? result.explanationSections
                : [];
            sections.replaceChildren(...explanationSections.map(createExplanationSection));
        }
        panel.hidden = false;
    }

    function createExplanationSection(section) {
        const sectionType = section.sectionType || "text";
        const wrapper = document.createElement("section");
        wrapper.className = `quiz-reading-learning-aid quiz-reading-learning-aid-layout--${sectionType}`;
        wrapper.dataset.sectionType = sectionType;
        wrapper.dataset.sectionTitle = section.title || "";

        if (section.title) {
            const heading = document.createElement("h5");
            heading.textContent = section.title;
            wrapper.append(heading);
        }

        if (sectionType === "table") {
            wrapper.append(createAccessTable(section.entries, section.tableHeader === true));
        } else if (sectionType === "diagram") {
            wrapper.append(createDiagram(section.entries));
        } else if (sectionType === "examples") {
            wrapper.append(createExamples(section.entries));
        } else if (sectionType === "qa" || sectionType === "comparison") {
            wrapper.append(createQa(section.entries));
        } else if (sectionType === "list") {
            wrapper.append(createExplanationList(section.entries));
        } else if (sectionType === "official-references") {
            wrapper.append(createOfficialReferences(section.officialReferences));
        } else {
            wrapper.append(createExplanationText(section.entries));
        }
        return wrapper;
    }

    function createOfficialReferences(references) {
        const list = document.createElement("ul");
        list.className = "quiz-reading-official-references";
        references.forEach((reference) => {
            const item = document.createElement("li");
            const link = document.createElement("a");
            link.href = reference.uri;
            link.target = "_blank";
            link.rel = "noopener noreferrer";
            link.textContent = `${reference.sourceName} ${reference.sectionNumber} — ${reference.sectionTitle}`;
            const description = textElement("p", reference.description);
            item.append(link, description);
            list.append(item);
        });
        return list;
    }

    function createExplanationList(entries) {
        const list = document.createElement("ul");
        list.className = "quiz-reading-section-list";
        entries.forEach((entry) => {
            const item = document.createElement("li");
            item.textContent = `${entry.before}${entry.emphasis}${entry.after}`;
            list.append(item);
        });
        return list;
    }

    function createAccessTable(entries, hasHeader) {
        const table = document.createElement("div");
        table.className = "quiz-reading-access-table";
        table.setAttribute("role", "table");
        entries.forEach((entry, index) => {
            const row = document.createElement("div");
            row.setAttribute("role", "row");
            if ((hasHeader && index === 0) || entry.tableHeader === true) {
                row.classList.add("quiz-reading-access-row--header");
            }
            if (entry.highlighted) row.classList.add("quiz-reading-access-row--focus");
            if (entry.detail) {
                row.classList.add("quiz-reading-access-row--five-columns");
            } else if (entry.after) {
                row.classList.add("quiz-reading-access-row--four-columns");
            } else if (entry.emphasis) {
                row.classList.add("quiz-reading-access-row--three-columns");
            }
            if (!entry.label && !entry.emphasis) row.classList.add("quiz-reading-access-row--no-label");
            row.append(
                textElement("code", entry.label, "cell"),
                textElement("span", entry.before, "cell")
            );
            if (entry.emphasis) row.append(textElement("span", entry.emphasis, "cell"));
            if (entry.after) row.append(textElement("span", entry.after, "cell"));
            if (entry.detail) row.append(textElement("span", entry.detail, "cell"));
            table.append(row);
        });
        return table;
    }

    function createDiagram(entries) {
        const diagram = document.createElement("div");
        diagram.className = "quiz-reading-concept-flow";
        entries.forEach((entry) => {
            const row = document.createElement("div");
            row.append(textElement("code", entry.label), textElement("span", "→"), textElement("span", entry.before));
            diagram.append(row);
        });
        return diagram;
    }

    function createExamples(entries) {
        const examples = document.createElement("div");
        examples.className = "quiz-reading-name-examples";
        entries.forEach((entry) => {
            const row = document.createElement("div");
            if (!entry.label) {
                const note = textElement("p", entry.before);
                note.className = "quiz-reading-examples-note";
                row.append(note);
            } else {
                row.append(textElement("code", entry.label), textElement("span", "→"), textElement("code", entry.before));
            }
            examples.append(row);
        });
        return examples;
    }

    function createQa(entries) {
        const qa = document.createElement("div");
        qa.className = "quiz-reading-qa";
        entries.forEach((entry) => {
            const row = document.createElement("p");
            row.append(textElement("strong", entry.label), textElement("span", entry.before));
            if (entry.emphasis) {
                row.classList.add("quiz-reading-comparison-row--three-columns");
                row.append(textElement("span", entry.emphasis));
            }
            qa.append(row);
        });
        return qa;
    }

    function createExplanationText(entries) {
        const text = document.createElement("div");
        text.className = "quiz-reading-section-text";
        entries.forEach((entry) => {
            const paragraph = document.createElement("p");
            if (entry.label) paragraph.append(textElement("strong", `${entry.label} `));
            paragraph.append(document.createTextNode(entry.before));
            if (entry.emphasis) paragraph.append(textElement("strong", entry.emphasis));
            paragraph.append(document.createTextNode(entry.after));
            text.append(paragraph);
        });
        return text;
    }

    function textElement(tagName, value, role) {
        const element = document.createElement(tagName);
        element.textContent = value;
        if (role) element.setAttribute("role", role);
        return element;
    }

    function enableNextStep(stepId) {
        if (!stepId) return;
        const nextButton = document.querySelector(
            `.quiz-circuit-code-button[data-code-step="${CSS.escape(stepId)}"]`
        );
        if (!nextButton) return;
        nextButton.dataset.answerEnabled = "true";
        nextButton.disabled = false;
        nextButton.setAttribute("aria-disabled", "false");
        nextButton.setAttribute("aria-pressed", "false");
        nextButton.classList.remove("quiz-circuit-code-button--locked");
        nextButton.classList.add("quiz-circuit-code-button--next");
        const nextCircuitStep = nextButton.closest(".quiz-part-circuit-step");
        if (nextCircuitStep) {
            nextCircuitStep.classList.remove("quiz-part-circuit-step--locked");
            nextCircuitStep.classList.add("quiz-part-circuit-step--next");
        }
    }

    function updateProgress(completedCount) {
        const progress = document.querySelector("[data-part-progress]");
        if (progress) {
            progress.textContent = `完了 ${completedCount} / ${progress.dataset.partTotal}`;
        }
    }

    function showPartComplete() {
        const completion = document.querySelector("[data-part-complete]");
        if (completion) completion.hidden = false;
    }

    async function revealCorrectMeaning(result) {
        const target = document.querySelector("[data-current-meaning-slot]");
        if (!target) {
            return;
        }

        target.replaceChildren(createMeaning(result.meaning));
        target.classList.add("quiz-reading-code-meaning--visible");
        await wait(reduceMotion ? 20 : 190);
    }

    async function lightBulb(stepId) {
        const step = document.querySelector(
            `[data-bulb-step="${CSS.escape(stepId)}"]`
        );
        if (!step) {
            return;
        }

        step.classList.add(
            "quiz-part-circuit-step--completed",
            "quiz-part-circuit-step--lighting"
        );
        await wait(reduceMotion ? 20 : 120);
        step.classList.remove("quiz-part-circuit-step--lighting");
    }

    function createMeaning(meaning) {
        const label = document.createElement("span");
        label.className = "quiz-reading-code-meaning-label quiz-reading-code-meaning-label--arrived";
        label.textContent = meaning;
        return label;
    }

    function wait(milliseconds) {
        return new Promise((resolve) => {
            window.setTimeout(resolve, milliseconds);
        });
    }

    function showIncorrect(button) {
        button.classList.add("answer-option--incorrect", "quiz-answer-shake");
        const message = document.getElementById("quiz-reading-live-error");
        if (message) {
            message.hidden = false;
        }
        window.setTimeout(() => {
            button.classList.remove("answer-option--incorrect", "quiz-answer-shake");
            answering = false;
            optionArea.setAttribute("aria-busy", "false");
            setAnswerCardsDisabled(false);
        }, reduceMotion ? 100 : 800);
    }
})();
