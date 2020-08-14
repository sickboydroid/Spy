# Save mapping for understanding stack traces
-printmapping build/outputs/mapping/release/mapping.txt

# This is necessary to keep the JGit libary working
-keepnames class org.eclipse.jgit.internal.** { *; }
