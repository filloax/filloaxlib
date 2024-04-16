#!/usr/bin/env node

const core = require('@actions/core');
const fs = require('fs');

try {
  const gradlePropertiesPath = core.getInput('gradle.properties', { required: true });
  const gradlePropertiesContent = fs.readFileSync(gradlePropertiesPath, 'utf8');

  const versionRegex = /mod_version\s*=\s*([^\s]+)/;
  const versionMatch = gradlePropertiesContent.search(versionRegex);

  if (versionMatch && versionMatch[1]) {
    core.setOutput('VERSION', versionMatch[1]);
    console.log("Found version", versionMatch[1]);
  } else {
    throw new Error('Could not find version in gradle.properties');
  }
} catch (error) {
  core.setFailed(error.message);
}