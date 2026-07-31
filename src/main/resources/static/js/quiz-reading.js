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
            setAnswerCardsDisabled(true);

            try {
                // submitボタンのname/valueはFormData(form)だけでは含まれないため、
                // 実際に押されたカードの識別情報を明示して送ります。
                const body = new URLSearchParams();
                body.set(button.name, button.value);
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

                await placeCorrectCard(button, result);
                window.location.reload();
            } catch (error) {
                // 判定が届いた可能性もあるため、二重POSTせず再表示します。
                window.location.reload();
            }
        });
    });

    function setAnswerCardsDisabled(disabled) {
        forms.forEach((form) => {
            const button = form.querySelector("button");
            if (button) {
                button.disabled = disabled;
                button.setAttribute("aria-disabled", String(disabled));
            }
        });
    }

    async function placeCorrectCard(sourceButton, result) {
        const target = document.querySelector(
            `[data-meaning-slot="${CSS.escape(result.answeredStepId)}"]`
        );
        if (!target) {
            return;
        }

        if (!reduceMotion) {
            await flyCard(sourceButton, target);
        }

        target.replaceChildren(createFixedCard(result.meaning));
        target.closest(".quiz-reading-code-unit")
            ?.classList.add("quiz-reading-code-unit--completed");
        lightBulb(result.answeredStepId);
    }

    function flyCard(sourceButton, target) {
        const sourceRect = sourceButton.getBoundingClientRect();
        const targetRect = target.getBoundingClientRect();
        const flyingCard = sourceButton.cloneNode(true);

        flyingCard.removeAttribute("name");
        flyingCard.removeAttribute("value");
        flyingCard.classList.add("quiz-reading-flying-card");
        Object.assign(flyingCard.style, {
            left: `${sourceRect.left}px`,
            top: `${sourceRect.top}px`,
            width: `${sourceRect.width}px`,
            height: `${sourceRect.height}px`
        });
        document.body.appendChild(flyingCard);

        const targetX = targetRect.left
            + (targetRect.width - sourceRect.width) / 2
            - sourceRect.left;
        const targetY = targetRect.top
            + (targetRect.height - sourceRect.height) / 2
            - sourceRect.top;

        return flyingCard.animate(
            [
                { transform: "translate(0, 0) scale(0.9)", opacity: 1 },
                { transform: `translate(${targetX}px, ${targetY}px) scale(0.78)`, opacity: 0.96 }
            ],
            {
                duration: 650,
                easing: "cubic-bezier(0.22, 0.8, 0.3, 1)",
                fill: "forwards"
            }
        ).finished.finally(() => flyingCard.remove());
    }

    function createFixedCard(meaning) {
        const card = document.createElement("span");
        card.className = "quiz-reading-fixed-card quiz-reading-fixed-card--arrived";
        card.textContent = meaning;
        return card;
    }

    function lightBulb(stepId) {
        document.querySelector(`[data-bulb-step="${CSS.escape(stepId)}"]`)
            ?.classList.add("quiz-part-circuit-step--completed");
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
            setAnswerCardsDisabled(false);
        }, reduceMotion ? 100 : 800);
    }
})();
