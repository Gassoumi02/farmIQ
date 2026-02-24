# Setting Up Claude Skills for the VS Code Agent (Claude Code)

This guide shows how to set up [Claude Skills](https://github.com/ComposioHQ/awesome-claude-skills) for **Claude Code** (the VS Code / terminal AI agent) and how to create a local skill for the FarmIQ project.

---

## What Are Claude Skills?

Claude Skills are portable workflow files that teach Claude how to perform specific, repeatable tasks. A skill is simply a folder containing a `SKILL.md` file with YAML frontmatter. Once installed, Claude Code automatically activates the relevant skill when you ask it something related.

---

## Prerequisites

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
