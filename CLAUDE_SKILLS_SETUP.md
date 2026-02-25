# Setting Up Claude Skills for the VS Code Agent (Claude Code)

This guide shows how to set up [Claude Skills](https://github.com/ComposioHQ/awesome-claude-skills) for **Claude Code** (the VS Code / terminal AI agent) and how to create a local skill for the FarmIQ project.

---

## What Are Claude Skills?

Claude Skills are portable workflow files that teach Claude how to perform specific, repeatable tasks. A skill is simply a folder containing a `SKILL.md` file with YAML frontmatter. Once installed, Claude Code automatically activates the relevant skill when you ask it something related.

---

## Prerequisites

> **Want a free option?** Skip this section and jump to [Using a Free Agent Instead of a Paid API Key](#using-a-free-agent-instead-of-a-paid-api-key) below.

The steps in Options A–C use Claude Code which requires a paid Anthropic API key. The prerequisites are:

| Tool | Version | Install |
|---|---|---|
| [Claude Code CLI](https://docs.anthropic.com/en/docs/claude-code) | latest | `npm install -g @anthropic-ai/claude-code` |
| Node.js | 18+ | [nodejs.org](https://nodejs.org) |
| An Anthropic API key | — | [console.anthropic.com](https://console.anthropic.com) |

Verify the installation:
```bash
claude --version
```

---

## Using a Free Agent Instead of a Paid API Key

You do **not** need a paid subscription to benefit from an AI coding agent on this project. The three free options below cover most use cases.

### Free Option 1 — GitHub Copilot Free + Custom Instructions (recommended)

GitHub Copilot has a **free tier** (no credit card required) that includes:
- 2,000 code completions per month
- 50 chat messages per month
- Access to the Copilot agent in VS Code

**Step 1 — Enable GitHub Copilot Free**

1. Go to [github.com/features/copilot](https://github.com/features/copilot) and click **Start for free**.
2. Sign in with your GitHub account — no credit card needed.
3. In VS Code, install the **[GitHub Copilot](https://marketplace.visualstudio.com/items?itemName=GitHub.copilot)** extension.
4. Sign in when prompted and select the **Free** plan.

**Step 2 — Project-specific instructions are already set up**

This repository ships a `.github/copilot-instructions.md` file that GitHub Copilot reads automatically. It tells Copilot about the FarmIQ project structure, build commands, coding conventions, and more — for free, with no extra configuration.

```text
.github/
└── copilot-instructions.md   ← loaded automatically by Copilot
```

Open VS Code in the project root and Copilot will immediately understand:
- How to build and run the app (`mvn javafx:run`)
- The MVCS layer pattern (Model → DAO → Service → Controller)
- Log4j2 logging style, custom exception pattern, JDBC conventions

**Step 3 — Use Copilot Chat in VS Code**

Press `Ctrl+Alt+I` (or open the Copilot Chat panel) and ask questions like:
- *"How do I add a new DAO for the Parcelle model?"*
- *"Show me how to log an error using the project's convention."*
- *"What's the database connection string format for this project?"*

---

### Free Option 2 — GitHub Copilot Free via CLI

If you prefer the terminal, install the GitHub CLI extension:

```bash
# Install GitHub CLI (if not already installed)
# https://cli.github.com/

# Install the Copilot extension
gh extension install github/gh-copilot

# Ask a question
gh copilot suggest "how do I run this JavaFX app with Maven"

# Explain a command
gh copilot explain "mvn javafx:run"
```

The same `.github/copilot-instructions.md` file is used here automatically.

---

### Free Option 3 — Fully Local (Ollama + Continue.dev, no internet required)

Run a private, 100% free AI agent locally using open-source models:

**Step 1 — Install Ollama**
```bash
# Linux / macOS
curl -fsSL https://ollama.com/install.sh | sh

# Windows: download from https://ollama.com/download
```

**Step 2 — Pull a coding model** (choose one based on your RAM)
```bash
ollama pull codellama        # 4 GB RAM minimum — good for code
ollama pull llama3.2         # 2 GB RAM minimum — fast general purpose
ollama pull deepseek-coder   # 4 GB RAM minimum — strong at Java
```

**Step 3 — Install the Continue.dev VS Code extension**
Install **[Continue](https://marketplace.visualstudio.com/items?itemName=Continue.continue)** from the VS Code marketplace.

**Step 4 — Point Continue at Ollama**
Continue auto-detects a running Ollama instance. Open Continue's config (`~/.continue/config.json`) and verify:
```json
{
  "models": [
    {
      "title": "CodeLlama (local)",
      "provider": "ollama",
      "model": "codellama"
    }
  ]
}
```

**Step 5 — Add project context to Continue**
Create a `.continuerc.json` in the project root to give Continue the same context as the Copilot instructions:
```json
{
  "contextProviders": [
    { "name": "file", "params": { "nFiles": 10 } },
    { "name": "codebase" }
  ],
  "customCommands": [
    {
      "name": "farmiq",
      "description": "FarmIQ project assistant",
      "prompt": "You are an expert on the FarmIQ JavaFX + Java 17 + MySQL project. Use Maven to build (`mvn clean compile`) and run (`mvn javafx:run`). Follow the MVCS pattern: Model → DAO → Service → Controller. Use Log4j2 with {} placeholders for logging."
    }
  ]
}
```

Now type `@farmiq` in the Continue chat to activate the FarmIQ-aware context.

---

### Free Tier Comparison

| Option | Cost | Internet required | Monthly limits |
|---|---|---|---|
| GitHub Copilot Free | Free | Yes | 2,000 completions / 50 chats |
| GitHub CLI Copilot | Free | Yes | Same as above |
| Ollama + Continue.dev | Free | No (after download) | Unlimited |
| Claude Code (paid) | ~$20/month | Yes | Unlimited |
| Composio plugin | Free tier available | Yes | Limited actions |

> **Recommendation**: Start with **GitHub Copilot Free** — it requires zero configuration beyond the free GitHub account you already have, and `.github/copilot-instructions.md` is already set up in this repo.

---

## Option A — Install a Skill from awesome-claude-skills

The [ComposioHQ/awesome-claude-skills](https://github.com/ComposioHQ/awesome-claude-skills) repository is a curated list of ready-made skills. To install one locally:

### 1. Clone or download the repository
```bash
git clone https://github.com/ComposioHQ/awesome-claude-skills.git /tmp/awesome-claude-skills
```

### 2. Create the Claude Code skills directory
```bash
mkdir -p ~/.config/claude-code/skills/
```

### 3. Copy the skill you want
```bash
# Example: copy the "skill-creator" skill
cp -r /tmp/awesome-claude-skills/skill-creator ~/.config/claude-code/skills/
```

### 4. Verify the skill metadata
```bash
head ~/.config/claude-code/skills/skill-creator/SKILL.md
```

### 5. Restart Claude Code and try it
```bash
claude
```
Claude will automatically load all skills in `~/.config/claude-code/skills/` and activate the relevant one based on your task.

---

## Option B — Connect Claude to 500+ Apps (Composio)

The `connect-apps` plugin lets Claude send emails, create GitHub issues, post to Slack, and more:

### 1. Launch Claude Code with the plugin
```bash
claude --plugin-dir ./connect-apps-plugin
```

### 2. Run the setup command inside Claude Code
```
/connect-apps:setup
```
Paste your free API key from [platform.composio.dev](https://platform.composio.dev) when prompted.

### 3. Restart and verify
```bash
exit
claude
```
Ask Claude to send you a test email — if it works, Claude is connected.

---

## Option C — Create a Local Skill (this project)

This repository already ships a ready-made FarmIQ developer skill.

### 1. Create the Claude Code skills directory (if it does not exist)
```bash
mkdir -p ~/.config/claude-code/skills/
```

### 2. Install the FarmIQ skill
```bash
cp -r claude-skills/farmiq-dev ~/.config/claude-code/skills/
```

### 3. Verify the skill was picked up
```bash
head ~/.config/claude-code/skills/farmiq-dev/SKILL.md
```

### 4. Start Claude Code from the project root
```bash
claude
```

Claude will now understand the FarmIQ project structure, build commands, coding conventions, and database setup without you having to explain them each time.

---

## Creating Your Own Skill from Scratch

Any skill is just a folder with a `SKILL.md` file. The minimum template is:

```text
my-skill/
└── SKILL.md
```

**SKILL.md template:**
```markdown
---
name: my-skill-name
description: A clear description of what this skill does and when to use it.
---

# My Skill Name

## When to Use This Skill

- Use case 1
- Use case 2

## Instructions

[Detailed instructions for Claude on how to execute this skill]

## Examples

[Real-world examples showing the skill in action]
```

**Tips from the awesome-claude-skills contributors:**
- Focus on specific, repeatable tasks
- Write instructions *for Claude*, not end users
- Include clear examples and edge cases
- Document prerequisites and dependencies
- Test across Claude.ai, Claude Code, and the API

Place your finished skill folder into `~/.config/claude-code/skills/` and restart Claude Code.

---

## Skill Locations Quick Reference

| Platform | Skill directory |
|---|---|
| Claude Code (all OS) | `~/.config/claude-code/skills/<skill-name>/` |
| Claude.ai | Upload via the 🧩 icon in the chat interface |
| Claude API | Pass `skills: ["skill-id"]` in your API request |

---

## Resources

- [ComposioHQ/awesome-claude-skills](https://github.com/ComposioHQ/awesome-claude-skills) — curated skill list
- [Anthropic/skills](https://github.com/anthropics/skills) — official example skills
- [Claude Skills User Guide](https://support.claude.com/en/articles/12512180-using-skills-in-claude)
- [Creating Custom Skills](https://support.claude.com/en/articles/12512198-creating-custom-skills)
- [Skills API Documentation](https://docs.claude.com/en/api/skills-guide)
- [Claude Community](https://community.anthropic.com)
- [GitHub Copilot Free tier](https://github.com/features/copilot) — free AI agent for VS Code
- [GitHub Copilot custom instructions](https://docs.github.com/en/copilot/customizing-copilot/adding-custom-instructions-for-github-copilot) — project-level context (`.github/copilot-instructions.md`)
- [Ollama](https://ollama.com) — run local AI models for free
- [Continue.dev](https://continue.dev) — free open-source AI coding assistant for VS Code
