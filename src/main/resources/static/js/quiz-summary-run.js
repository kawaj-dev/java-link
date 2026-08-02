(function () {
    "use strict";

    const terminal = document.querySelector("[data-summary-terminal]");
    const processPanel = document.querySelector("[data-summary-process]");
    if (!terminal || !processPanel) {
        return;
    }

    const primary = terminal.querySelector("[data-terminal-primary]");
    const processPrimary = processPanel.querySelector("[data-process-primary]");
    const processLabel = processPanel.querySelector("[data-process-label]");
    const processDescription = processPanel.querySelector(
        "[data-process-description]"
    );
    const complete = terminal.querySelector("[data-run-complete]");
    const output = terminal.dataset.consoleOutput || "Hello";
    const reducedMotion = window.matchMedia(
        "(prefers-reduced-motion: reduce)"
    ).matches;
    const compilingDuration = reducedMotion ? 450 : 1400;
    const runningDuration = reducedMotion ? 450 : 1200;
    const completeDelay = reducedMotion ? 200 : 600;

    function showPhase(mainText, labelText, descriptionText) {
        processPrimary.textContent = mainText;
        processLabel.textContent = labelText;
        processDescription.textContent = descriptionText;
        terminal.hidden = true;
        processPanel.hidden = false;
        processPanel.classList.remove("quiz-summary-process-panel--visible");
        void processPanel.offsetWidth;
        processPanel.classList.add("quiz-summary-process-panel--visible");
    }

    function showOutput() {
        primary.textContent = output;
        processPanel.hidden = true;
        terminal.hidden = false;
        terminal.classList.remove("quiz-summary-terminal--visible");
        void terminal.offsetWidth;
        terminal.classList.add("quiz-summary-terminal--visible");
        window.setTimeout(() => {
            complete.hidden = false;
            complete.classList.add("quiz-summary-run-complete--visible");
            terminal.setAttribute("aria-busy", "false");
        }, completeDelay);
    }

    showPhase(
        "Compiling...",
        "コンパイル",
        "実行できる形に変換しています"
    );
    window.setTimeout(() => {
        showPhase(
            "Running...",
            "実行",
            "プログラムを動かしています"
        );
        window.setTimeout(showOutput, runningDuration);
    }, compilingDuration);
}());
