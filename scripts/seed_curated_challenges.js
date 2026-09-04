/**
 * Admin Seeding Script for Curated Cinephile Challenges
 *
 * Usage:
 *   node scripts/seed_curated_challenges.js [--dev]
 *
 * Requirements:
 *   - Service account JSON key pointed to by GOOGLE_APPLICATION_CREDENTIALS
 *   - Or initialized Firebase CLI admin session
 */

const admin = require("firebase-admin");
const fs = require("fs");
const path = require("path");

const isDev = process.argv.includes("--dev");
const collectionName = isDev ? "dev_curated_challenges" : "curated_challenges";
const documentId = "active_catalog";

const seedFilePath = path.join(
  __dirname,
  "..",
  "shared-data",
  "src",
  "main",
  "assets",
  "curated_challenges_seed.json"
);

if (!fs.existsSync(seedFilePath)) {
  console.error(`Seed file not found at: ${seedFilePath}`);
  process.exit(1);
}

try {
  admin.initializeApp();
} catch (e) {
  // If credentials missing, prompt helpful guidance
  console.error("Firebase admin failed to initialize. Ensure GOOGLE_APPLICATION_CREDENTIALS is set.");
  process.exit(1);
}

const db = admin.firestore();

async function seed() {
  console.log(`Reading seed catalog from ${seedFilePath}...`);
  const rawData = fs.readFileSync(seedFilePath, "utf8");
  const challenges = JSON.parse(rawData);

  console.log(`Seeding ${challenges.length} challenges into collection: "${collectionName}", document: "${documentId}"...`);

  const payload = {
    catalogVersion: 1,
    enabled: true,
    updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    challenges: challenges
  };

  await db.collection(collectionName).doc(documentId).set(payload);
  console.log(`Successfully seeded ${collectionName}/${documentId}!`);
}

seed().catch(err => {
  console.error("Error during seeding:", err);
  process.exit(1);
});
