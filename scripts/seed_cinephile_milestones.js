/**
 * Admin Seeding Script for Cinephile Milestones Catalog
 *
 * Usage:
 *   node scripts/seed_cinephile_milestones.js [--dev]
 *
 * Requirements:
 *   - Service account JSON key pointed to by GOOGLE_APPLICATION_CREDENTIALS
 *   - Or initialized Firebase CLI admin session
 */

const admin = require("firebase-admin");
const fs = require("fs");
const path = require("path");

const isDev = process.argv.includes("--dev");
const collectionName = isDev ? "dev_cinephile_milestones" : "cinephile_milestones";
const documentId = "active_catalog";

const seedFilePath = path.join(
  __dirname,
  "..",
  "shared-data",
  "src",
  "main",
  "assets",
  "cinephile_milestones_catalog.json"
);

if (!fs.existsSync(seedFilePath)) {
  console.error(`Seed file not found at: ${seedFilePath}`);
  process.exit(1);
}

try {
  admin.initializeApp();
} catch (e) {
  console.error("Firebase admin failed to initialize. Ensure GOOGLE_APPLICATION_CREDENTIALS is set.");
  process.exit(1);
}

const db = admin.firestore();

async function seed() {
  console.log(`Reading seed milestones catalog from ${seedFilePath}...`);
  const rawData = fs.readFileSync(seedFilePath, "utf8");
  const milestones = JSON.parse(rawData);

  console.log(`Seeding ${milestones.length} milestones into collection: "${collectionName}", document: "${documentId}"...`);

  const payload = {
    catalogVersion: 1,
    enabled: true,
    updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    milestones: milestones
  };

  await db.collection(collectionName).doc(documentId).set(payload);
  console.log(`Successfully seeded ${collectionName}/${documentId}!`);
}

seed().catch((err) => {
  console.error("Failed to seed milestones:", err);
  process.exit(1);
});
