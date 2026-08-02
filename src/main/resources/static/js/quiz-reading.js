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
        const term = panel.querySelector("[data-explanation-term]");
        const technical = panel.querySelector("[data-explanation-technical]");
        const beginner = panel.querySelector("[data-explanation-beginner]");
        if (term) term.textContent = result.technicalTerm;
        if (technical) technical.textContent = result.technicalExplanation;
        if (beginner) {
            beginner.replaceChildren(...result.beginnerExplanations.map((line) => {
                const paragraph = document.createElement("p");
                paragraph.textContent = line;
                return paragraph;
            }));
        }
        panel.hidden = false;
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
