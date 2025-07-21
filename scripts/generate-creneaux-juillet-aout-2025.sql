-- ============================================================================
-- Script de génération des créneaux pour JUILLET et AOÛT 2025
-- ============================================================================
-- 
-- Ce script génère automatiquement tous les créneaux pour juillet et août 2025
-- Règles appliquées :
-- - Lundi à Samedi uniquement (pas de dimanche)
-- - Horaires : 8h00 à 18h00
-- - Créneaux d'1 heure
-- - Pause déjeuner : 12h-14h (pas de créneaux)
-- - Jours fériés exclus : 14 juillet et 15 août
-- - Capacité : 2 véhicules par créneau
-- - Statut : disponible = true
--
-- Usage :
-- 1. PostgreSQL : psql -d garage_db -f generate-creneaux-juillet-aout-2025.sql
-- 2. H2 Console : Copier-coller ce script
-- 3. Via API : POST /api/admin/creneaux/generate/juillet-aout-2025
-- ============================================================================

-- Fonction utilitaire pour PostgreSQL (à adapter selon la base)
DO $$ 
DECLARE 
    current_date DATE;
    current_time TIME;
    next_time TIME;
    creneau_debut TIMESTAMP;
    creneau_fin TIMESTAMP;
    jour_semaine INTEGER;
    est_ferie BOOLEAN;
    compteur INTEGER := 0;
BEGIN
    -- Nettoyer les créneaux futurs existants (optionnel)
    -- DELETE FROM creneaux WHERE heure_debut > NOW();
    
    RAISE NOTICE '🚀 Début de génération des créneaux juillet-août 2025...';
    
    -- Parcourir tous les jours de juillet et août 2025
    current_date := '2025-07-01';
    
    WHILE current_date <= '2025-08-31' LOOP
        -- Obtenir le jour de la semaine (1=Lundi, 7=Dimanche)
        jour_semaine := EXTRACT(DOW FROM current_date);
        IF jour_semaine = 0 THEN jour_semaine = 7; END IF; -- Dimanche = 7
        
        -- Vérifier si c'est un jour férié
        est_ferie := current_date IN ('2025-07-14', '2025-08-15');
        
        -- Traiter seulement les jours ouvrables (lundi à samedi, hors fériés)
        IF jour_semaine BETWEEN 1 AND 6 AND NOT est_ferie THEN
            RAISE NOTICE '📅 Traitement du % (jour %)', current_date, jour_semaine;
            
            -- Générer les créneaux pour cette journée (8h-18h)
            current_time := '08:00:00';
            
            WHILE current_time < '18:00:00' LOOP
                next_time := current_time + INTERVAL '1 hour';
                
                -- Exclure la pause déjeuner (12h-14h)
                IF current_time NOT IN ('12:00:00', '13:00:00') THEN
                    creneau_debut := current_date + current_time;
                    creneau_fin := current_date + next_time;
                    
                    -- Insérer le créneau
                    INSERT INTO creneaux (heure_debut, heure_fin, disponible, capacite_totale) 
                    VALUES (creneau_debut, creneau_fin, true, 2);
                    
                    compteur := compteur + 1;
                END IF;
                
                current_time := next_time;
            END LOOP;
        ELSE
            IF est_ferie THEN
                RAISE NOTICE '🎉 Jour férié ignoré : %', current_date;
            ELSE
                RAISE NOTICE '⏭️  Dimanche ignoré : %', current_date;
            END IF;
        END IF;
        
        current_date := current_date + 1;
    END LOOP;
    
    RAISE NOTICE '✅ % créneaux générés avec succès !', compteur;
    RAISE NOTICE '📊 Répartition : Juillet + Août 2025, Lundi-Samedi, 8h-18h (hors 12h-14h)';
    RAISE NOTICE '🏢 Capacité : 2 véhicules par créneau';
    
END $$;

-- ============================================================================
-- Version simplifiée pour H2 ou bases ne supportant pas DO blocks
-- ============================================================================

/*
-- Décommentez cette partie si votre base ne supporte pas les blocs DO

-- Juillet 2025 - Jours ouvrables seulement
INSERT INTO creneaux (heure_debut, heure_fin, disponible, capacite_totale) VALUES
-- Mardi 1er juillet 2025
('2025-07-01 08:00:00', '2025-07-01 09:00:00', true, 2),
('2025-07-01 09:00:00', '2025-07-01 10:00:00', true, 2),
('2025-07-01 10:00:00', '2025-07-01 11:00:00', true, 2),
('2025-07-01 11:00:00', '2025-07-01 12:00:00', true, 2),
-- Pause déjeuner 12h-14h
('2025-07-01 14:00:00', '2025-07-01 15:00:00', true, 2),
('2025-07-01 15:00:00', '2025-07-01 16:00:00', true, 2),
('2025-07-01 16:00:00', '2025-07-01 17:00:00', true, 2),
('2025-07-01 17:00:00', '2025-07-01 18:00:00', true, 2),

-- Répéter ce pattern pour tous les jours ouvrables...
-- (Cette approche manuelle est longue mais fonctionne partout)
*/

-- ============================================================================
-- Vérification des résultats
-- ============================================================================

-- Compter les créneaux générés par mois
SELECT 
    EXTRACT(YEAR FROM heure_debut) as annee,
    EXTRACT(MONTH FROM heure_debut) as mois,
    COUNT(*) as nombre_creneaux
FROM creneaux 
WHERE heure_debut >= '2025-07-01' AND heure_debut < '2025-09-01'
GROUP BY EXTRACT(YEAR FROM heure_debut), EXTRACT(MONTH FROM heure_debut)
ORDER BY annee, mois;

-- Compter les créneaux par jour de la semaine
SELECT 
    CASE EXTRACT(DOW FROM heure_debut)
        WHEN 1 THEN 'Lundi'
        WHEN 2 THEN 'Mardi' 
        WHEN 3 THEN 'Mercredi'
        WHEN 4 THEN 'Jeudi'
        WHEN 5 THEN 'Vendredi'
        WHEN 6 THEN 'Samedi'
        WHEN 0 THEN 'Dimanche'
    END as jour_semaine,
    COUNT(*) as nombre_creneaux
FROM creneaux 
WHERE heure_debut >= '2025-07-01' AND heure_debut < '2025-09-01'
GROUP BY EXTRACT(DOW FROM heure_debut)
ORDER BY EXTRACT(DOW FROM heure_debut);

-- Afficher les créneaux de la première semaine de juillet 2025
SELECT 
    id,
    heure_debut,
    heure_fin,
    disponible,
    capacite_totale,
    EXTRACT(DOW FROM heure_debut) as dow,
    CASE EXTRACT(DOW FROM heure_debut)
        WHEN 1 THEN 'Lundi'
        WHEN 2 THEN 'Mardi' 
        WHEN 3 THEN 'Mercredi'
        WHEN 4 THEN 'Jeudi'
        WHEN 5 THEN 'Vendredi'
        WHEN 6 THEN 'Samedi'
        WHEN 0 THEN 'Dimanche'
    END as jour_semaine
FROM creneaux 
WHERE heure_debut >= '2025-07-01' AND heure_debut < '2025-07-08'
ORDER BY heure_debut;

-- ============================================================================
-- Notes d'utilisation
-- ============================================================================
-- 
-- 📋 RÉSUMÉ ATTENDU :
-- - Juillet 2025 : ~200 créneaux (27 jours ouvrables × 8 créneaux/jour, -14 juillet)
-- - Août 2025 : ~192 créneaux (26 jours ouvrables × 8 créneaux/jour, -15 août)  
-- - TOTAL : ~392 créneaux
--
-- 🛠️ PERSONNALISATION :
-- - Modifier les horaires : changer '08:00:00' et '18:00:00'
-- - Modifier la capacité : changer le '2' dans capacite_totale
-- - Ajouter des jours fériés : compléter la liste dans est_ferie
-- - Modifier la durée : changer INTERVAL '1 hour'
--
-- 🔧 MAINTENANCE :
-- - Exécuter ce script avant le début de la période
-- - Vérifier les résultats avec les requêtes de contrôle
-- - Adapter les jours fériés selon l'année
-- ============================================================================ 