package com.example.data

data class BioModel(
    val id: Int,
    val name: String,
    val category: String,
    val description: String,
    val pedagogicalValue: String,
    val infoPoints: List<String>,
    val features: List<String>
)

data class QuizQuestion(
    val id: Int,
    val modelId: Int,
    val question: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String
)

object BiologyData {
    val categories = listOf(
        "Cell Biology",
        "Genetics",
        "Human Anatomy",
        "Plant Biology",
        "Microbiology"
    )

    val models = listOf(
        BioModel(
            id = 1,
            name = "Human Cell (3D Interactive Cell)",
            category = "Cell Biology",
            description = "Explore the tiny building block of human life. Zoom in to interact with specialized structures called organelles that power, clean, and sustain the microscopic system.",
            pedagogicalValue = "Teaches cell structure, organelle specialization, protein production, metabolic energy (ATP) synthesis, and core physiological differences between animal and plant cellular structures.",
            infoPoints = listOf(
                "Cell Membrane: Acts as a semi-permeable double layer (phospholipid bilayer) regulating substance entry and exit.",
                "Cytoplasm: The jelly-like fluid that fills the cell, suspends organelles, and supports chemical reactions.",
                "Nucleus: The command center containing DNA, shielding genetic material, and directing cellular activities.",
                "Mitochondria: The powerhouses of the cell where cellular respiration occurs, generating ATP energy.",
                "Ribosomes: Tiny RNA-rich granules responsible for translating genetic codes into essential proteins.",
                "Endoplasmic Reticulum (ER): Rough ER synthesizes proteins using attached ribosomes; Smooth ER produces lipids.",
                "Golgi Apparatus: Modifies, sorts, and packages macromolecules like proteins and lipids for secretion or delivery.",
                "Lysosomes: Spherical sacs containing digestive enzymes to break down waste materials, cellular debris, and foreign invaders.",
                "Vacuoles: Small storage vesicles used for sequestering waste products and water transport in animal cells.",
                "Cell Types: Animal cells lack rigid cell walls and chloroplasts, allowing flexible shapes unlike plant cells."
            ),
            features = listOf(
                "Rotate Organelles",
                "Tap-to-Learn Labels",
                "Cell Beating/Pulsing Animation",
                "Zoom into Nucleus"
            )
        ),
        BioModel(
            id = 2,
            name = "DNA Double Helix",
            category = "Genetics",
            description = "The blueprint of life. Rotate the twisted ladder of genetic engineering. Study base pairings that construct biological instructions for all living things.",
            pedagogicalValue = "Covers molecular biology constants: nitrogenous base pair complementarities, chromosome packing, replication mechanics, gene reading, inheritance, and transcription.",
            infoPoints = listOf(
                "DNA Structure: A double stranded polymer forming a right-handed twisted ladder shape called a double helix.",
                "Base Pairs: Consists of complementary hydrogen-bonded nitrogenous bases: Adenine to Thymine (A-T) and Guanine to Cytosine (G-C).",
                "Chromosomes: Extremely long, continuous strands of DNA wrapped tightly around histone proteins for structural packaging.",
                "Genetic Coding: The precise sequence of base triplets (codons) forms unique instructions for building specific amino acid chains.",
                "DNA Replication: The semi-conservative replication process where strands unzip and double to produce two identical DNA molecules.",
                "Mutations: Alterations in nucleotide sequences caused by copying errors or environmental factors like UV light.",
                "RNA Transcription: The mechanism where a segment of DNA is copied into temporary single-stranded messenger RNA (mRNA).",
                "Protein Synthesis: The cellular translation process where ribosomes read mRNA strands to build active functional proteins.",
                "Heredity: The passing down of genetic sequences from parents to offspring, establishing traits.",
                "Human Genome: Comprises 3 billion base pairs organized into 23 pairs of chromosomes, encoding about 20,000 genes."
            ),
            features = listOf(
                "Helix Rotation",
                "Animated Replication (Unzipping)",
                "Color-Coded Base Highlighting",
                "Gene Highlighting Zoom"
            )
        ),
        BioModel(
            id = 3,
            name = "Human Heart",
            category = "Human Anatomy",
            description = "A muscular pump that never stops. Track oxygenated and deoxygenated blood pathways as they pulse through ventricles, atria, and flexible valves in real-time.",
            pedagogicalValue = "Explains cardiovascular physiology, blood aeration flow, muscular chambers, valve integrity, high/low pressure cycles, and cardiac electrical conduction.",
            infoPoints = listOf(
                "Heart Chambers: Divided into four chambers: two upper atria (receiving blood) and two lower ventricles (pumping blood).",
                "Blood Circulation: Moves through two paths: Pulmonary circulation to the lungs, and Systemic circulation to the body.",
                "Oxygenated Blood: Leaves lungs, enters the left atrium via pulmonary veins, flows to left ventricle, and enters aorta to feed the body.",
                "Deoxygenated Blood: Returns from body organs, enters the right atrium via vena cava, and is pumped from right ventricle to lungs.",
                "Valves Function: Four internal valves (Tricuspid, Pulmonary, Mitral, Aortic) prevent backflow, ensuring one-way blood movement.",
                "Heartbeat Cycle: Separated into Systole (contraction phase pumping blood out) and Diastole (relaxation phase filling with blood).",
                "Arteries and Veins: Arteries carry blood away from the heart (typically oxygen-rich); Veins return blood to the heart.",
                "Blood Pressure: The physical force exerted by circulating blood on walls of blood vessels, measured in Systolic/Diastolic.",
                "ECG Basics: An Electrocardiogram (ECG/EKG) captures electrical waves (P, QRS, T) generated by pacemaker cells triggering beats.",
                "Heart Diseases: Includes coronary artery blocks, valve failures, and high blood pressure, leading to heart strain."
            ),
            features = listOf(
                "Rhythmic Beating Animation",
                "Oxygen/Deoxygen Flow Particles",
                "Heart Wall Layer Removal",
                "Interactive Valve Inspection"
            )
        ),
        BioModel(
            id = 4,
            name = "Human Brain",
            category = "Human Anatomy",
            description = "The central command center of thoughts, sensory data, and motor controls. Select distinct lobes to view electrical networks and cognitive functions.",
            pedagogicalValue = "Focuses on neural structures, specialized brain regions (cerebrum, cerebellum, brainstem), sensory inputs, memory retention, and chemical communication.",
            infoPoints = listOf(
                "Cerebrum: The largest division of the brain, handling higher-level thought, language, reasoning, and voluntary actions.",
                "Cerebellum: Located at the lower back of the brain, coordinating fine motor muscle balance, posture, and coordination.",
                "Brain Stem: Connects the cerebrum to the spine; manages life-essential automatic actions like breathing and heart rate.",
                "Nervous System: The central command hub, receiving electrical inputs from sensory nerves and broadcasting motor replies.",
                "Memory Function: Split into short-term (hippocampus) and long-term (cerebral cortex) networks across neural circuits.",
                "Sensory Processing: Different specialized hubs receive and decode sight (occipital), sound (temporal), and touch (parietal).",
                "Motor Control: The primary motor cortex coordinates physical muscles via signals sent down the spinal column.",
                "Brain Lobes: Formed of 4 major lobes: Frontal (decisions), Parietal (touch), Temporal (memory/audio), and Occipital (vision).",
                "Neurotransmitters: Chemical messengers (Dopamine, Serotonin) that cross gaps (synapses) to transmit brain signals.",
                "Brain Disorders: Conditions like Alzheimer's, stroke, or concussion that alter neural pathways, memory, and cognitive action."
            ),
            features = listOf(
                "Brain Activity Glow (Neural Fire)",
                "Interactive Region Selection",
                "Nerve Pathway Pulse Animation",
                "Transparent Tissue Layers"
            )
        ),
        BioModel(
            id = 5,
            name = "Neuron & Synapse",
            category = "Human Anatomy",
            description = "Witness electricity traveling through the body's building blocks. Spark impulses along axons and watch neurotransmitters cross synaptic gaps in slow motion.",
            pedagogicalValue = "Teaches neurophysiology, electrical impulse propagation (action potentials), chemical synapse mechanics, reflex arcs, and interconnected brain-body networks.",
            infoPoints = listOf(
                "Neuron Structure: Consists of a cell body with nucleus, receiving dendrite branches, and an output axon fiber.",
                "Dendrites: Tree-like branch receptors that receive biochemical signals from adjacent neurons and feed them to the soma.",
                "Axon: A long, slender cable wrapped in protective myelin sheaths that conducts electrical signals away from the cell body.",
                "Synapse: The microscopic junction gap between the axon terminal of one neuron and the dendrite receiver of another.",
                "Electrical Impulses: Action potentials caused by swift shifts in sodium and potassium ions rushing across the cell membrane.",
                "Neurotransmitters: Chemical keys packed in vesicles, released at synapses to bind with receptors and trigger new pulses.",
                "Reflex Actions: Bullet-fast automatic safety movements processed purely in the spinal cord without brain delay.",
                "Signal Transmission: Operates as an electrical pulse inside a single neuron, but swaps to chemical keys inside the synapse.",
                "Brain Communication: Tiny, coordinated pulses form thoughts, memories, learning reactions, and mood levels.",
                "Neural Networks: Trillions of complex, plastic synapses that continuously rewrite connections as we learn new subjects."
            ),
            features = listOf(
                "Action Potential Pulse Line",
                "Chemical Synapse Close-up",
                "Ion Flow Animation",
                "Slow-Motion Learning Mode"
            )
        ),
        BioModel(
            id = 6,
            name = "Human Skeleton",
            category = "Human Anatomy",
            description = "The scaffolding that supports and protects. Rotate the skeleton, isolate critical bones like the femur or clavicle, and toggle on internal X-ray features.",
            pedagogicalValue = "Covers structural support, organ protection, mineral storage (calcium), hematopoietic bone marrow blood production, joint biomechanics, and fracture recovery.",
            infoPoints = listOf(
                "Bone Names: Over 206 bones in adults, including the femur (longest), stapes (smallest), skeleton core, and limbs.",
                "Bone Functions: Establishes body shape, permits structural movement, shields internal soft organs, and stores minerals.",
                "Joint Types: Includes specialized hinge joints (knee), ball-and-socket joints (hip), and pivot joints (neck).",
                "Skull Anatomy: Constructed of hard cranium bone plates protecting the brain, joined together by fused sutures.",
                "Spine Structure: Comprises 33 interlocking vertebrae shielding the delicate spinal cord while providing flexible torso bending.",
                "Rib Cage Protection: Extends around chest cavities to form a sturdy cage shielding the vulnerable lungs and heart.",
                "Bone Marrow: Soft tissue inside spongy bones consisting of red marrow (making red/white blood cells) and yellow marrow.",
                "Calcium Storage: Bones hold 99% of body calcium, releasing it into blood streams dynamically to keep muscles functioning.",
                "Fractures: Bone breaks that heal via specialized collagen nets hardening into solid calcium matrices.",
                "Muscle Attachment: Striated muscles tie to bones via tough tendons, acting as levers to pull joint segments."
            ),
            features = listOf(
                "Full-Body 3D Rotation",
                "Individual Bone Isolator",
                "X-Ray Style Shader Toggle",
                "Joint Range Motion Sim"
            )
        ),
        BioModel(
            id = 7,
            name = "Digestive System",
            category = "Human Anatomy",
            description = "Track the journey of nutrients. Watch complex food break down mechanically and chemically from the stomach down to small intestine absorption.",
            pedagogicalValue = "Explains mechanical vs chemical digestion, peristaltic pathways, stomach acidity, enzyme synthesis, liver/pancreas filters, and nutrient assimilation.",
            infoPoints = listOf(
                "Mouth Digestion: Mechanical chew grinding mixed with saliva-amylase enzymes starting immediate starch breakdown.",
                "Esophagus: A muscular tube directing swallowed food to the stomach via rhythmic wave contractions called peristalsis.",
                "Stomach Acids: A strong acid vault (pH 1.5-2) utilizing hydrochloric acid and pepsin enzymes to melt proteins down.",
                "Liver Function: Synthesizes golden-green bile fluids to emulsify fats, while detoxifying blood returning from the stomach.",
                "Pancreas Role: Manufactures critical digestive enzymes (lipase, amylase) and sodium bicarbonate to neutralize stomach acids.",
                "Small Intestine: Where 90% of absorption occurs, using millions of tiny, finger-like villi capillaries.",
                "Large Intestine: Gathers remaining indigestible mass, reabsorbing excess water and housing friendly gut microbes.",
                "Nutrients Overview: Soluble carbohydrates, lipids, amino acids, and minerals extracted during transit to sustain cellular life.",
                "Enzymes: Biological catalysts (lactase, protease) that accelerate targeted macromolecule chemical splitting.",
                "Digestive Disorders: Includes ulcers, acid reflux, and celiac disease where intestinal lining folds get inflamed."
            ),
            features = listOf(
                "Food Bolus Journey Animation",
                "Interactable Organ Callouts",
                "Acid Splash/Neutralizer Sim",
                "Villi Absorption Zoom"
            )
        ),
        BioModel(
            id = 8,
            name = "Respiratory System",
            category = "Human Anatomy",
            description = "Breathe in, breathe out. Follow air particles moving down the windpipe, inflating flexible chest cavities, and exchanging oxygen at microscopic alveoli clusters.",
            pedagogicalValue = "Covers breathing physics (diaphragm contraction), trachea/bronchi filtration, lung capacities, alveoli blood filtration, and O2/CO2 gas exchanges.",
            infoPoints = listOf(
                "Nose Function: Filters dust with tiny hairs, while warming and humidifying cold air before entering lungs.",
                "Trachea: The rigid windpipe protected by cartilege rings, carrying air cleanly from larynx to bronchial pathways.",
                "Lungs Anatomy: Light, spongy bilateral organs filling the chest cavity; right lung has 3 lobes, left has 2.",
                "Bronchial Tree: The trachea branches out into left/right bronchi, splitting into tiny bronchioles like tree limbs.",
                "Alveoli: Millions of microscopic, bubble-like air sacs wrapped in capillary screens where gas swaps occur.",
                "Oxygen Exchange: Inhaled oxygen diffuses from alveoli air bubbles across thin single-cell membranes into blood capillaries.",
                "Breathing Physics: Driven by volume changes: contracting the diaphragm drops chest pressure, drawing air inward.",
                "Diaphragm Role: A dome-shaped muscle under the ribs that pulls down to inhale, and relaxes upward to exhale.",
                "CO2 Extraction: Waste carbon dioxide diffuses from blood cells back into alveoli sacs to be blown out of the mouth.",
                "Lung Diseases: Conditions like asthma (airway swelling) or emphysema (alveoli destruction) that restrict airflow."
            ),
            features = listOf(
                "Breathing Expansion Animation",
                "O2/CO2 Particle Visualizer",
                "Alveoli Capillary Zoom",
                "Diaphragm Action Control"
            )
        ),
        BioModel(
            id = 9,
            name = "Plant Cell",
            category = "Plant Biology",
            description = "Under the microscope of botany. Tap the chloroplasts to initiate carbon assimilation and explore the high-pressure water vacuole that props plants upward.",
            pedagogicalValue = "Highlights botanical cell differences: rigid cellulose cell walls, large turgor pressure vacuoles, photosynthesis pathways, solar collection, and starches.",
            infoPoints = listOf(
                "Cell Wall: A exterior armor layer made of sturdy cellulose fibers providing scaffolding rigidity and protection.",
                "Chloroplasts: Specialized solar-panel organelles filled with green chlorophyll molecules capturing sunlight wavelength bands.",
                "Photosynthesis: The chemical factory transforming sunlight, water, and CO2 into chemical glucose sugars and oxygen byproduct.",
                "Large Vacuole: A huge water-filled balloon occupying up to 90% of cell volume, applying turgor pressure to prevent wilting.",
                "Plant DNA: Packaged in the nucleus, but plant chloroplasts and mitochondria also retain their own independent DNA rings.",
                "Water Transport: Driven by root vacuum suction pulling water columns up plant stems to satisfy cellular needs.",
                "Energy Systems: Glucose made in chloroplasts is converted into reusable ATP fuel inside plant mitochondria.",
                "Sunlight Importance: Light acts as the initial photon spark to split water water molecules, releasing free electrons.",
                "Plant Growth: Cells expand as high internal water turgor forces stretch young cell walls before they calcify.",
                "Animal Comparison: Plant cells feature rigid rectangular shapes, cell walls, vacuoles, and chloroplasts, absent in animal cells."
            ),
            features = listOf(
                "Photosynthesis Sunlight Tap",
                "Turgor Pressure Slider",
                "Chlorophyll Molecular Zoom",
                "Water Flow Visualizer"
            )
        ),
        BioModel(
            id = 10,
            name = "Immune System / WBC",
            category = "Microbiology",
            description = "A battlefield inside our bodies. Watch a White Blood Cell detect active viruses, deploy Y-shaped antibodies, and engulf foreign bacteria.",
            pedagogicalValue = "Covers leukocyte types, antigen binding, antibody synthesis, active vs passive defenses, viral replication disruption, and immune memory.",
            infoPoints = listOf(
                "White Blood Cells: Crucial mobile defense cells (leukocytes) seeking and destroying foreign microbial trespassers.",
                "Antibodies: Y-shaped proteins custom-engineered to lock onto specific invader antigens, neutralizing them.",
                "Viruses: Microscopic protein capsules containing genes that hijack host body cells to replicate copy waves.",
                "Bacteria Defense: Extracellular bacteria are targeted by neutrophils and macrophages, swallowing them whole.",
                "Vaccines: Injecting harmless deactivated antigens to teach cells to form antibodies before a real attack.",
                "Immune Response: Includes rapid generic inflammation defenses and slower tailored antibody cellular seek missions.",
                "T Cells: Killer T-cells hunt and execute infected body cells, while Helper T-cells coordinate general defense campaigns.",
                "Fever Mechanism: Brain raises blood temperature to disrupt heat-sensitive viral cloning while accelerating leukocyte activity.",
                "Infection Cycle: Pathogen enters, multiplies, triggers host tissue cellular distress, gets neutralized, and cleared.",
                "Autoimmune Diseases: Errors where white blood cells misidentify normal self-tissues as foreign threats and assault them."
            ),
            features = listOf(
                "Virus Attack Survival Simulator",
                "Antibody Firing Launcher",
                "leukocyte Phagocytosis (Eating)",
                "Host-Defense Timeline Chart"
            )
        )
    )

    val quizQuestions = listOf(
        QuizQuestion(
            id = 1,
            modelId = 1,
            question = "Which organelle is known as the command center containing the cell's genetic blueprint (DNA)?",
            options = listOf("Mitochondria", "Nucleus", "Ribosome", "Golgi Apparatus"),
            correctAnswerIndex = 1,
            explanation = "The Nucleus houses genetic DNA and serves as the command center directing critical cell processes."
        ),
        QuizQuestion(
            id = 2,
            modelId = 2,
            question = "What are the complementary nitrogenous base pairs that bind the DNA double helix together?",
            options = listOf("A-C and G-T", "A-G and T-C", "Adenine-Thymine (A-T) and Guanine-Cytosine (G-C)", "Adenine-Uracil (A-U) and Thymine-Cytosine (T-C)"),
            correctAnswerIndex = 2,
            explanation = "DNA base pairs always match Adenine to Thymine (A-T) and Guanine to Cytosine (G-C) via hydrogen bonds."
        ),
        QuizQuestion(
            id = 3,
            modelId = 3,
            question = "Which chamber of the heart pumps highly oxygenated blood out into the body's main arterial highway (aorta)?",
            options = listOf("Right Atrium", "Right Ventricle", "Left Atrium", "Left Ventricle"),
            correctAnswerIndex = 3,
            explanation = "The thick-walled Left Ventricle pumps oxygenated blood with high pressure into the aorta to feed systemic body tissues."
        ),
        QuizQuestion(
            id = 4,
            modelId = 4,
            question = "Which major lobe of the brain is primarily tasked with decision making, strategic reasoning, and voluntary muscle action?",
            options = listOf("Occipital Lobe", "Frontal Lobe", "Temporal Lobe", "Parietal Lobe"),
            correctAnswerIndex = 1,
            explanation = "The Frontal Lobe is the seat of executive decisions, personality control, language expression, and motor actions."
        ),
        QuizQuestion(
            id = 5,
            modelId = 5,
            question = "How is an electrical neural message conducted in the gap (synapse) between separate brain cells?",
            options = listOf("Via lightning arcs", "Via physical wires", "Via chemical neurotransmitter messenger ligands", "Via sound pressure waves"),
            correctAnswerIndex = 2,
            explanation = "Inside the synapse, the electrical action potential is converted into chemical keys called neurotransmitters which diffuse across the gap."
        ),
        QuizQuestion(
            id = 6,
            modelId = 6,
            question = "What is the primary hematopoietic function of soft tissue (red bone marrow) inside spongy skeletons?",
            options = listOf("Filtering carbon dioxide", "Producing red and white blood cells", "Storing extra calcium deposits", "Generating digestive hormones"),
            correctAnswerIndex = 1,
            explanation = "Red bone marrow contains stem cells that generate new red blood cells, white blood cells, and blood platelets."
        ),
        QuizQuestion(
            id = 7,
            modelId = 7,
            question = "Where does the vast majority (around 90%) of digestive fluid nutrient and water absorption take place?",
            options = listOf("Stomach", "Mouth", "Small Intestine", "Large Intestine"),
            correctAnswerIndex = 2,
            explanation = "The massive surface area of the Small Intestine, covered with microscopic villi capillaries, absorbs 90% of meal nutrients."
        ),
        QuizQuestion(
            id = 8,
            modelId = 8,
            question = "Which thin-walled, balloon-like air clusters serve as the actual site of oxygen and carbon dioxide diffusion into capillaries?",
            options = listOf("Alveoli", "Trachea rings", "Bronchioles", "Pleural membrane"),
            correctAnswerIndex = 0,
            explanation = "Alveoli are microscopic, high-surface-area air sacs wrapped in blood vessel meshes where breathing gas diffusion occurs."
        ),
        QuizQuestion(
            id = 9,
            modelId = 9,
            question = "What botanical cellular structure occupies up to 90% of plant volumes, inflating cells like turgid balloons to prevent wilting?",
            options = listOf("Chloroplast", "Large Central Vacuole", "Cell Wall", "Cytoplasm"),
            correctAnswerIndex = 1,
            explanation = "The Large Central Vacuole applies high water turgor pressure outward against rigid cell walls, keeping plants upright."
        ),
        QuizQuestion(
            id = 10,
            modelId = 10,
            question = "How do custom-engineered Y-shaped antibody proteins assist white blood cell actions in terminating viral active phases?",
            options = listOf("By dissolving whole muscles", "By binding onto targets, neutralizing antigens and tagging viruses for destruction", "By cloning host genetic maps", "By heating up general blood flow pathways"),
            correctAnswerIndex = 1,
            explanation = "Antibodies lock onto viral antigens, neutralizing their infiltration keys and tagging aggregate particles for phagocytes to eat."
        )
    )
}
